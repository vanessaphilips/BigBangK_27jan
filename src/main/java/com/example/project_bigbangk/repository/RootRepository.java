// Created by RayS
// Creation date 3-12-2021

/* Ray: RootRepository alvast gemaakt, bleek een methode in mijn voorbeelden te zitten waarin die werd aangeroepen.
 * Dus voor Address-Service maar gelijk meegenomen
 */

package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.*;
import com.example.project_bigbangk.model.Orders.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class RootRepository {

    private IClientDAO clientDAO;
    private IAddressDAO addressDAO;
    private IWalletDAO walletDAO;
    private IAssetDAO assetDAO;
    private JdbcOrderDAO orderDAO;
    private final IPricedateDAO priceDateDAO;
    private final int AMOUNT_OF_ASSETS = 20;

    public RootRepository(IClientDAO clientDAO, IAddressDAO addressDAO, IWalletDAO walletDAO,
                          IPricedateDAO priceDateDAO, IAssetDAO assetDAO, JdbcOrderDAO orderDAO) {
        this.clientDAO = clientDAO;
        this.addressDAO = addressDAO;
        this.walletDAO = walletDAO;
        this.priceDateDAO = priceDateDAO;
        this.assetDAO = assetDAO;
        this.orderDAO = orderDAO;
    }

    // CLIENT
    //heb wallet ook ingezet. -philip
    public Client findClientByEmail(String email) {
        Client client = clientDAO.findClientByEmail(email);
        if (client != null) {
            client.setAddress(findAddressByEmail(email));
            client.setWallet(findWalletByEmail(email));
        }
        return client;
    }

    public Address findAddressByEmail(String email) {
        return addressDAO.findAddressByEmail(email);
    }

    /**
     * Saves address, wallet and client seperately in Database.
     *
     * @param client the client object
     */
    public void createNewlyRegisteredClient(Client client) {
        addressDAO.saveAddress(client.getAddress());
        walletDAO.saveNewWallet(client.getWallet());
        for (Asset asset : client.getWallet().getAssets().keySet()) {
            walletDAO.createWalletAsset(client.getWallet(), asset, client.getWallet().getAssets().get(asset));
        }
        clientDAO.saveClient(client);
    }

    /**
     * Saves a List of priceHistory to the DataBase
     *
     * @param priceHistories the List of priceHistory to be saved
     * @author Pieter Jan Bleichrodt
     */
    //PriceHistory
    public void savePriceHistories(List<PriceHistory> priceHistories) {
        boolean saveAssets = assetDAO.getNumberOfAssets() != AMOUNT_OF_ASSETS;
        for (PriceHistory priceHistory : priceHistories) {
            if (saveAssets) {
                assetDAO.saveAsset(priceHistory.getAsset());
            }
            for (PriceDate priceDate : priceHistory.getPriceDates()) {
                priceDateDAO.savePriceDate(priceDate, priceHistory.getAsset().getCode());
            }
        }
    }

    /**
     * retrieves the currentPrice of a given Asset from the DB
     *
     * @param assetCode the code of the Asset to be retrieved
     * @return the currentPrice as a double
     * @author Pieter Jan Bleichrodt
     */
    public double getCurrentPriceByAssetCode(String assetCode) {
        return priceDateDAO.getCurrentPriceByAssetCode(assetCode);
    }

    /**
     * retrieves the pricehistories for all Assets from the database
     *
     * @param dateTime date in past for defining the interval for which priceHistoryData is retrieved
     * @return a list of PriceHistory
     */
    public List<PriceHistory> getAllPriceHistories(LocalDateTime dateTime) {
        List<Asset> assets = assetDAO.getAllAssets();
        List<PriceHistory> priceHistories = new ArrayList<>();
        if (assets != null) {
            for (Asset asset : assets) {
                List<PriceDate> priceDates = priceDateDAO.getPriceDatesByCodeFromDate(dateTime, asset.getCode());
                if (priceDates != null) {
                    asset.setCurrentPrice(Collections.max(priceDates).getPrice());
                    new PriceHistory(priceDates, asset);
                    priceHistories.add(new PriceHistory(priceDates, asset));
                }
            }
        }
        if (priceHistories.size() != 0) {
            return priceHistories;
        }
        return null;
    }

    //Asset

    /**
     * retrieves all Assets from DB
     *
     * @author Pieter Jan Bleichrodt
     */
    public List<Asset> getAllAssets() {
        List<Asset> assets = assetDAO.getAllAssets();
        if (assets != null) {
            for (Asset asset : assets) {
                setCurrentPriceOfAsset(asset);
            }
        }
        return assets;
    }

    private Asset findAssetByOrderId(int orderId) {
        Asset asset = assetDAO.findAssetByOrderId(orderId);
        setCurrentPriceOfAsset(asset);
        return asset;
    }

    /**
     * find an Asset by its code
     *
     * @param code code of the asset in String format
     * @return an Asset with matching code
     */
    public Asset findAssetByCode(String code) {
        Asset asset = assetDAO.findAssetByCode(code);
        setCurrentPriceOfAsset(asset);
        return asset;
    }

    private void setCurrentPriceOfAsset(Asset asset) {
        if (asset != null) {
            asset.setCurrentPrice(priceDateDAO.getCurrentPriceByAssetCode(asset.getCode()));
        }
    }
    // WALLET

    public Wallet findWalletByEmail(String email) {
        Wallet wallet = walletDAO.findWalletByEmail(email);
        if (wallet != null) {
            fillWalletWithTransactions(wallet);
            fillOrderListsInWallet(wallet);
            fillWalletWithAssetsAmount(wallet);
        }
        return wallet;
    }

    private void fillWalletWithTransactions(Wallet wallet) {
        List<Transaction> transactions = orderDAO.findAllTransactionsByIban(wallet.getIban());
        for (Transaction transaction : transactions) {
            transaction.setAsset(findAssetByOrderId((int) transaction.getOrderId()));
            Wallet sellerWallet = walletDAO.FindSellerWalletByOrderId((int) transaction.getOrderId());
            Wallet buyerWallet = walletDAO.FindBuyerWalletByOrderId((int) transaction.getOrderId());
            transaction.setSellerWallet(sellerWallet);
            transaction.setBuyerWallet(buyerWallet);
        }
        wallet.setTransaction(transactions);
    }

    private void fillOrderListsInWallet(Wallet wallet) {
        List<AbstractOrder> abstractOrders = orderDAO.findOrdersByWallet(wallet);
        abstractOrders.forEach(ao -> ao.setAsset(findAssetByOrderId(ao.getOrderId())));
        List<Limit_Buy> limit_buys = abstractOrders.stream().filter(ao -> ao instanceof Limit_Buy).map(ao -> (Limit_Buy) ao).collect(Collectors.toList());
        List<Limit_Sell> limit_sells = abstractOrders.stream().filter(ao -> ao instanceof Limit_Sell).map(ao -> (Limit_Sell) ao).collect(Collectors.toList());
        List<Stoploss_Sell> stoploss_sells = abstractOrders.stream().filter(ao -> ao instanceof Stoploss_Sell).map(ao -> (Stoploss_Sell) ao).collect(Collectors.toList());
        Wallet stopInfiniteLoopWallet = walletDAO.findWalletByIban(wallet.getIban());
        limit_buys.forEach(lb -> lb.setBuyer(stopInfiniteLoopWallet));
        limit_sells.forEach(ls -> ls.setSeller(stopInfiniteLoopWallet));
        stoploss_sells.forEach(sl -> sl.setSeller(stopInfiniteLoopWallet));
        wallet.setLimitBuy(limit_buys);
        wallet.setLimitSell(limit_sells);
        wallet.setStoplossSell(stoploss_sells);
    }


    public Wallet findWalletbyBankCode(String bankCode) {
        Wallet wallet = walletDAO.findWalletByBankCode(bankCode);
        if (wallet != null) {
            fillWalletWithTransactions(wallet);
            fillOrderListsInWallet(wallet);
            fillWalletWithAssetsAmount(wallet);
        }
        return wallet;
    }

    public Wallet findWalletByIban(String iban) {
        Wallet wallet = walletDAO.findWalletByIban(iban);
        if (wallet != null) {
            fillWalletWithTransactions(wallet);
            fillOrderListsInWallet(wallet);
            fillWalletWithAssetsAmount(wallet);
        }
        return wallet;
    }

    public void updateWalletBalanceAndAsset(Wallet wallet, Asset asset, double amount) {
        walletDAO.updateBalance(wallet);
        walletDAO.updateWalletAssets(wallet, asset, amount);
    }

    public Wallet findWalletWithAssetByIban(String iban) {
        Wallet wallet = walletDAO.findWalletByIban(iban);
        if (wallet == null) {
            return wallet;
        }
        fillWalletWithTransactions(wallet);
        fillOrderListsInWallet(wallet);
        fillWalletWithAssetsAmount(wallet);
        return wallet;
    }

    public Wallet findWalletByTransactionID(int orderId) {
        Wallet wallet = walletDAO.FindBuyerWalletByOrderId(orderId);
        if (wallet == null) {
            wallet = walletDAO.FindSellerWalletByOrderId(orderId);
        }
        if (wallet != null) {
            fillWalletWithTransactions(wallet);
            fillOrderListsInWallet(wallet);
            fillWalletWithAssetsAmount(wallet);
        }
        return wallet;
    }

    private void fillWalletWithAssetsAmount(Wallet wallet) {
        Map<Asset, Double> assetWithAmountMap = new HashMap<>();
        List<Asset> assets = assetDAO.getAllAssets();
        for (Asset asset : assets) {
            assetWithAmountMap.put(assetDAO.findAssetByCode(asset.getCode()), walletDAO.findAmountOfAsset(wallet.getIban(), asset.getCode()));
        }
        wallet.setAssets(assetWithAmountMap);
    }

    //Order

    /**
     * for retreiving all Limit_Sell orders
     *
     * @return List<Limit_Sell> with all limit_Sell orders
     */
    public boolean deleteOrderByID(int orderId) {
        return orderDAO.deleteOrderById(orderId);
    }

    public boolean updateLimitSell(Limit_Sell limit_sell) {
        return orderDAO.updateLimitSell(limit_sell);
    }

    public boolean updateLimitBuy(Limit_Buy limit_buy) {
        return orderDAO.updateLimitBuy(limit_buy);
    }

    public boolean updateStopLoss(Stoploss_Sell stoploss_sell) {
        return orderDAO.updateStopLoss(stoploss_sell);
    }

    public List<Limit_Sell> getAllLimitSells() {
        List<Limit_Sell> limit_sells = orderDAO.getAllLimitSells();
        for (Limit_Sell limit_sell : limit_sells) {
            limit_sell.setSeller(findWalletByTransactionID(limit_sell.getOrderId()));
            limit_sell.setAsset(findAssetByOrderId(limit_sell.getOrderId()));
        }
        return limit_sells;
    }

    /**
     * for retreiving all Limit_buy orders
     *
     * @return List<Limit_Buy> with all limit_Buy orders
     */
    public List<Limit_Buy> getAllLimitBuys() {
        List<Limit_Buy> limit_buys = orderDAO.getAllLimitBuys();
        for (Limit_Buy limit_buy : limit_buys) {
            limit_buy.setBuyer(findWalletByTransactionID(limit_buy.getOrderId()));
            limit_buy.setAsset(findAssetByOrderId(limit_buy.getOrderId()));
        }
        return limit_buys;
    }

    /**
     * for retreiving all Stoploss_Sell orders
     *
     * @return List<Stoploss_Sell> with all Stoploss_Sell orders
     */
    public List<Stoploss_Sell> getAllStopLossSells() {
        List<Stoploss_Sell> stoploss_sells = orderDAO.getAllStopLossSells();
        for (Stoploss_Sell stoploss_sell : stoploss_sells) {
            stoploss_sell.setSeller(findWalletByTransactionID(stoploss_sell.getOrderId()));
            stoploss_sell.setAsset(findAssetByOrderId(stoploss_sell.getOrderId()));
        }
        return stoploss_sells;
    }

    //ORDER > TRANSACTION

    /**
     * Saves Transaction, including sellerWallet and buyerWallet in database.
     *
     * @param transaction transaction object
     */

    public void saveTransaction(Transaction transaction) {
        orderDAO.saveTransaction(transaction);
        walletDAO.updateBalance(transaction.getBuyerWallet());
        walletDAO.updateWalletAssets(transaction.getBuyerWallet(), transaction.getAsset(), transaction.getBuyerWallet().getAssets().get(transaction.getAsset()));
        walletDAO.updateBalance(transaction.getSellerWallet());
        walletDAO.updateWalletAssets(transaction.getSellerWallet(), transaction.getAsset(), transaction.getSellerWallet().getAssets().get(transaction.getAsset()));
    }

    /**
     * Deze methode vult niet de currentPrice in Asset
     *
     * @param client
     * @deprecated use the wallet in Client object
     */

    public void fillWalletWithTransactions(Client client) {
        List<Transaction> transactions = orderDAO.findAllTransactionsByIban(client.getWallet().getIban());
        for (Transaction transaction : transactions) {
            int orderId = (int) transaction.getOrderId();
            transaction.setAsset(assetDAO.findAssetByOrderId(orderId));
            transaction.setSellerWallet(walletDAO.FindSellerWalletByOrderId(orderId));
            transaction.setBuyerWallet(walletDAO.FindBuyerWalletByOrderId(orderId));
        }
        client.getWallet().setTransaction(transactions);
    }

    //ORDER > LIMIT_BUY

    /**
     * Saves Limit_Buy order temporary. To be completed when there is a match with another client's offer -> matchservice.
     *
     * @param limit_buy author = Vanessa Philips
     */
    public void saveLimitBuyOrder(Limit_Buy limit_buy) {
        orderDAO.saveLimit_Buy(limit_buy);
    }

    //ORDER > LIMIT_SELL

    /**
     * Saves Limit_Sell order temporary. To be completed when there is a match with another client's offer -> matchservice).
     *
     * @param limit_sell author = Vanessa Philips
     */
    public void saveLimitSellOrder(Limit_Sell limit_sell) {
        orderDAO.saveLimit_Sell(limit_sell);
    }

    //ORDER > STOPLOSS_SELL

    /**
     * Saves Stoploss_Sell order temporary. To be completed when there is a match with another offer (bank) -> matchservice.
     *
     * @param stoploss_sell author = Vanessa Philips
     */
    public void saveStoploss_Sell(Stoploss_Sell stoploss_sell) {
        orderDAO.saveStoploss_Sell(stoploss_sell);
    }


}

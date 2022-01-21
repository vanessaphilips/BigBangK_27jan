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

    public double getCurrentPriceByAssetCode(String assetCode) {
        return priceDateDAO.getCurrentPriceByAssetCode(assetCode);
    }

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
    public List<Asset> getAllAssets() {
        List<Asset> assets = assetDAO.getAllAssets();
        if (assets != null) {
            for (Asset asset : assets) {
                asset.setCurrentPrice(priceDateDAO.getCurrentPriceByAssetCode(asset.getCode()));
            }
        }
        return assets;
    }

    public Asset findAssetByCode(String code) {
        return assetDAO.findAssetByCode(code);
    }

    // WALLET

    public Wallet findWalletByEmail(String email) {
        Wallet wallet = walletDAO.findWalletByEmail(email);
        return findWalletWithAssetByIban(wallet.getIban());
    }

    public Wallet findWalletbyBankCode(String bankCode) {
        Wallet wallet = walletDAO.findWalletByBankCode(bankCode);
        return findWalletWithAssetByIban(wallet.getIban());
    }

    public Wallet findWalletByIban(String iban) {
        return walletDAO.findWalletByIban(iban);
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
        fillWalletWithAssets(wallet);
        return wallet;
    }

    public Wallet findWalletByOrderID(int orderId) {
        Wallet wallet = walletDAO.FindBuyerWalletByOrderId(orderId);
        if (wallet == null) {
            wallet = walletDAO.FindSellerWalletByOrderId(orderId);
        }
        if (wallet != null) {
            fillWalletWithAssets(wallet);
        }
        return wallet;
    }

    private void fillWalletWithAssets(Wallet wallet) {
        Map<Asset, Double> assetWithAmountMap = new HashMap<>();
        List<Asset> assets = assetDAO.getAllAssets();
        for (Asset asset : assets) {
            assetWithAmountMap.put(assetDAO.findAssetByCode(asset.getCode()), walletDAO.findAmountOfAsset(wallet.getIban(), asset.getCode()));
        }
        wallet.setAssets(assetWithAmountMap);
    }

    //Order
    public boolean deleteOrderByID(int orderId) {
        return orderDAO.deleteOrderById(orderId);
    }

    public boolean updateLimitSell(Limit_Sell limit_sell) {
        return orderDAO.updateLimitSell(limit_sell);
    }

    public boolean updateLimitBuy(Limit_Buy limit_buy) {
        return orderDAO.updateLimitBuy(limit_buy);
    }


    public List<Limit_Sell> getAllLimitSell() {
        List<Limit_Sell> limit_sells = orderDAO.getAllLimitSells();
        for (Limit_Sell limit_sell : limit_sells) {
            limit_sell.setSeller(findWalletByOrderID(limit_sell.getOrderId()));
        }
        return limit_sells;
    }

    public List<Limit_Buy> getAllLimitBuy() {
        List<Limit_Buy> limit_buys = orderDAO.getAllLimitBuys();
        for (Limit_Buy limit_buy : limit_buys) {
            limit_buy.setBuyer(findWalletByOrderID(limit_buy.getOrderId()));
        }
        return limit_buys;
    }
    public List<Stoploss_Sell> getAllStopLossSells() {
        List<Stoploss_Sell> stoploss_sells = orderDAO.getAllStopLossSells();
        for (Stoploss_Sell stoploss_sell: stoploss_sells) {
            stoploss_sell.setSeller(findWalletByOrderID(stoploss_sell.getOrderId()));
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

    public void fillWalletWithTransactions(Client client){
        Wallet clientWallet = client.getWallet();
        clientWallet.setTransaction(orderDAO.findAllTransactionsByIban(clientWallet.getIban()));
        for (Transaction transaction:clientWallet.getTransaction()) {
            transaction.setBuyerWallet(walletDAO.FindBuyerWalletByOrderId((int) transaction.getOrderId()));
            transaction.setSellerWallet(walletDAO.FindSellerWalletByOrderId((int) transaction.getOrderId()));
            transaction.setAsset(assetDAO.findAssetByOrderId((int) transaction.getOrderId()));
        }
    }

    //ORDER > LIMIT_BUY

    /**
     * Saves Limit_Buy order temporary. To be completed when there is a match with another client's offer -> matchservice.
     * @param limit_buy author = Vanessa Philips
     */
    public void saveLimitBuyOrder(Limit_Buy limit_buy){
        orderDAO.saveLimit_Buy(limit_buy);
    }

    //ORDER > LIMIT_SELL

    /**
     * Saves Limit_Sell order temporary. To be completed when there is a match with another client's offer -> matchservice).
     * @param limit_sell author = Vanessa Philips
     */
    public void saveLimitSellOrder(Limit_Sell limit_sell){
        orderDAO.saveLimit_Sell(limit_sell);
    }

    //ORDER > STOPLOSS_SELL

    /**
     * Saves Stoploss_Sell order temporary. To be completed when there is a match with another offer (bank) -> matchservice.
     * @param stoploss_sell author = Vanessa Philips
     */
    public void saveStoploss_Sell(Stoploss_Sell stoploss_sell){
        orderDAO.saveStoploss_Sell(stoploss_sell);
    }

}

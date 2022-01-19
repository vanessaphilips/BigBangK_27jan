package com.example.project_bigbangk.service;
/*

@Author Philip Beeltje, Studentnummer: 500519452
*/

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.OrderDTO;
import com.example.project_bigbangk.model.Orders.Limit_Buy;
import com.example.project_bigbangk.model.Orders.Limit_Sell;
import com.example.project_bigbangk.model.Orders.Transaction;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class Orderservice {

    double currentAssetPrice;
    private Asset asset;
    private RootRepository rootRepository;
    private Client client;

    public Orderservice(RootRepository rootRepository) {
        this.rootRepository = rootRepository;
    }

    public enum Messages
    {
        FundClient("Order Failed: Client has insufficient funds."),
        FundBank("Order Failed: Bank has insufficient funds."),
        AssetClient("Order Failed: Client has insufficient assets."),
        AssetBank("Order Failed: Bank has insufficient assets."),
        SuccessBuy("Buy-Order successful"),
        SuccessSell("Sell-Order successful"),
        WaitingLimitBuy("Limit-buy order waiting for match"),
        WaitingLimitSell("Limit-sell order waiting for match");
        private String body;

        Messages(String envBody) {
            this.body = envBody;
        }
        public String getBody() {
            return body;
        }
    }

    public String handleOrderByType(OrderDTO order, Client clientFromToken){
        client = clientFromToken;
        currentAssetPrice = rootRepository.getCurrentPriceByAssetCode(order.getAssetCode());
        asset = rootRepository.findAssetByCode(order.getAssetCode());

        // types:
        // BuyOrder (alleen met bank)   code: Buy       -klaar
        // SellOrder (alleen met bank)  code: Sell      -klaar
        // Limit_Buy                    code: Lbuy      -sprint 3
        // Limit_Sell                   code: Lsell     -sprint 3
        // Stoploss_Sell                code: Sloss     -sprint 3

        if(order.getOrderType().equals("Buy")){
            return checkBuyOrder(order);
        }
        if(order.getOrderType().equals("Sell")){
            return checkSellOrder(order);
        }
        if(order.getOrderType().equals("Lbuy")) {
            return checkLbuyOrder(order);
        }
        if(order.getOrderType().equals("Lsell")) {
            return checkLsellOrder(order);
        }
        if(order.getOrderType().equals("Sloss")) {
            return checkSlossOrder(order);
        }
            return "Incorrect order type in JSON";
    }

    // BuyOrder (alleen met bank) -> code: Buy

    public String checkBuyOrder(OrderDTO order){
        double priceExcludingFee = order.getAssetAmount() * currentAssetPrice;
        double orderFee = priceExcludingFee * BigBangkApplicatie.bigBangk.getFeePercentage();
        double totalCost = priceExcludingFee + orderFee;
        Wallet clientWallet = client.getWallet();
        Wallet bankWallet = rootRepository.findWalletbyBankCode(BigBangkApplicatie.bigBangk.getCode());

        if(clientWallet.getBalance() >= totalCost){
            if(bankWallet.getAssets().get(asset) >= order.getAssetAmount()){
                executeBuyOrder(order, priceExcludingFee, orderFee, totalCost, clientWallet, bankWallet);
                return Messages.SuccessBuy.getBody();
                
            } else{
                return Messages.AssetBank.getBody();
            }
         } else {
            return Messages.FundClient.getBody();
        }
    }

    private void executeBuyOrder(OrderDTO order, double priceExcludingFee, double orderFee, double totalCost, Wallet clientWallet, Wallet bankWallet) {
        clientWallet.setBalance(clientWallet.getBalance()- totalCost);
        clientWallet.getAssets().replace(asset, clientWallet.getAssets().get(asset) + order.getAssetAmount());

        bankWallet.setBalance(bankWallet.getBalance() + totalCost);
        bankWallet.getAssets().replace(asset, bankWallet.getAssets().get(asset) - order.getAssetAmount());

        Transaction transaction = new Transaction(asset, priceExcludingFee , order.getAssetAmount(), LocalDateTime.now(), orderFee, clientWallet, bankWallet);
        sendOrderToDatabase(clientWallet, bankWallet, transaction);
    }

    // SellOrder (alleen met bank) -> code: Sell

    public String checkSellOrder(OrderDTO order){
        double sellOrderValue = order.getAssetAmount() * currentAssetPrice;
        double orderFee = sellOrderValue * BigBangkApplicatie.bigBangk.getFeePercentage();
        double totalPayout = sellOrderValue - orderFee;
        Wallet clientWallet = client.getWallet();
        Wallet bankWallet = rootRepository.findWalletbyBankCode(BigBangkApplicatie.bigBangk.getCode());

        if(bankWallet.getBalance() >= totalPayout) {
            if (clientWallet.getAssets().get(asset) >= order.getAssetAmount()) {
                executeSellOrder(order, sellOrderValue, orderFee, totalPayout, bankWallet, clientWallet);
                return Messages.SuccessSell.getBody();
            } else{
                return Messages.FundBank.getBody();
            }
        } else {
            return  Messages.AssetClient.getBody();
        }
    }

    private void executeSellOrder(OrderDTO order, double sellOrderValue, double orderFee, double totalPayout, Wallet clientWallet, Wallet bankWallet) {
        clientWallet.setBalance(clientWallet.getBalance() + totalPayout);
        clientWallet.getAssets().replace(asset, clientWallet.getAssets().get(asset) - order.getAssetAmount());

        bankWallet.setBalance(bankWallet.getBalance() - totalPayout);
        bankWallet.getAssets().replace(asset, bankWallet.getAssets().get(asset) + order.getAssetAmount());

        Transaction transaction = new Transaction(asset, sellOrderValue, order.getAssetAmount(), LocalDateTime.now(), orderFee, bankWallet, clientWallet);
        sendOrderToDatabase(clientWallet, bankWallet, transaction);
    }

    public void sendOrderToDatabase(Wallet walletOne, Wallet walletTwo, Transaction transaction){
        rootRepository.updateWalletBalanceAndAsset(walletOne, asset, walletOne.getAssets().get(asset));
        rootRepository.updateWalletBalanceAndAsset(walletTwo, asset, walletTwo.getAssets().get(asset));
        rootRepository.saveNewTransaction(transaction);
    }

    // Limit_Buy -> code: Lbuy

    /**
     * Checks if the Limit_Buy order can be done, if yes -> save waitingLimitBuyOrder in database
     * @param order
     * @return
     * author = Vanessa Philips
     */
    public String checkLbuyOrder(OrderDTO order) {
        double wantedAssetAmount = order.getAssetAmount() / currentAssetPrice;
        double orderFee = order.getAssetAmount() * BigBangkApplicatie.bigBangk.getFeePercentage();
        double totalCost = order.getAssetAmount() + orderFee;
        Wallet clientWallet = client.getWallet();

        if (clientWallet.getBalance() >= totalCost) {
            Limit_Buy limit_buy = new Limit_Buy(asset, order.getLimit(), wantedAssetAmount, LocalDateTime.now(), clientWallet);
            rootRepository.saveWaitingLimitBuyOrder(limit_buy);
        } else {
            return Messages.FundClient.getBody();
        }
        return Messages.WaitingLimitBuy.getBody();
    }

    // Limit_Sell -> code: Lsell

    /**
     * Checks if the Limit_Sell order can be done, if yes -> save waitingLimitSellOrder in database
     * @param order
     * author = Vanessa Philips
     * @return
     */
    public String checkLsellOrder(OrderDTO order){
        double offeredAssetAmount = order.getAssetAmount() / currentAssetPrice;
        Wallet clientWallet = client.getWallet();
        Asset asset = rootRepository.findAssetByCode(order.getAssetCode());

        if (clientWallet.getAssets().get(asset) >= offeredAssetAmount) {
            Limit_Sell limit_sell = new Limit_Sell(asset, order.getLimit(), offeredAssetAmount, LocalDateTime.now(), clientWallet);
            rootRepository.saveWaitingLimitSellOrder(limit_sell);
        } else {
            return Messages.AssetClient.getBody();
        }
        return Messages.WaitingLimitSell.getBody();
    }

    // Stoploss_Sell -> code: Sloss

    public String checkSlossOrder(OrderDTO order){
        return null;
    }

}

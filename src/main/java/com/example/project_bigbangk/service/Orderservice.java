package com.example.project_bigbangk.service;
/*

@Author Philip Beeltje, Studentnummer: 500519452
*/

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.OrderDTO;
import com.example.project_bigbangk.model.Orders.*;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class Orderservice {

    double currentAssetPrice;
    private Asset asset;
    private RootRepository rootRepository;
    private Wallet clientWallet;
    private Wallet bankWallet;

    public Orderservice(RootRepository rootRepository) {
        this.rootRepository = rootRepository;
    }

    public enum Messages {
        FundClient("Order Failed: Client has insufficient funds."),
        FundBank("Order Failed: Bank has insufficient funds."),
        AssetClient("Order Failed: Client has insufficient assets."),
        AssetBank("Order Failed: Bank has insufficient assets."),
        SuccessBuy("Buy-Order successful"),
        SuccessSell("Sell-Order successful"),
        WaitingLimitBuy("Limit-buy order saved and waiting for match"),
        WaitingLimitSell("Limit-sell order saved and waiting for match"),
        WaitingStoplossSell("Stoploss-sell order saved and waiting for match");
        private String body;

        Messages(String envBody) {
            this.body = envBody;
        }
        public String getBody() {
            return body;
        }
    }

    public String handleOrderByType(OrderDTO order, Client clientFromToken){
        clientWallet = clientFromToken.getWallet();
        bankWallet = rootRepository.findWalletbyBankCode(BigBangkApplicatie.bigBangk.getCode());
        currentAssetPrice = rootRepository.getCurrentPriceByAssetCode(order.getAssetCode());
        asset = rootRepository.findAssetByCode(order.getAssetCode());

        if(order.getOrderType().equals(TransactionType.BUY.toString())){
            return checkBuyOrder(order);
        }
        if(order.getOrderType().equals(TransactionType.SELL.toString())){
            return checkSellOrder(order);
        }
        if(order.getOrderType().equals(TransactionType.LIMIT_BUY.toString())) {
            return checkLbuyOrder(order);
        }
        if(order.getOrderType().equals(TransactionType.LIMIT_SELL.toString())) {
            return checkLsellOrder(order);
        }
        if(order.getOrderType().equals(TransactionType.STOPLOSS_SELL.toString())) {
            return checkSlossOrder(order);
        }
            return "Incorrect order type in JSON";
    }

    public String checkBuyOrder(OrderDTO order){
        double priceExcludingFee = order.getAssetAmount() * currentAssetPrice;
        double orderFee = priceExcludingFee * BigBangkApplicatie.bigBangk.getFeePercentage();
        double totalCost = priceExcludingFee + orderFee;

        if(clientWallet.sufficientBalance(totalCost)){
            if(bankWallet.sufficientAsset(asset, order.getAssetAmount())){
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
        clientWallet.removeFromBalance(totalCost);
        clientWallet.addToAsset(asset, order.getAssetAmount());

        bankWallet.addToBalance(totalCost);
        bankWallet.removeFromAsset(asset, order.getAssetAmount());

        Transaction transaction = new Transaction(asset, priceExcludingFee , order.getAssetAmount(), LocalDateTime.now(), orderFee, clientWallet, bankWallet);
        rootRepository.saveTransaction(transaction);
    }

    public String checkSellOrder(OrderDTO order){
        double sellOrderValue = order.getAssetAmount() * currentAssetPrice;
        double orderFee = sellOrderValue * BigBangkApplicatie.bigBangk.getFeePercentage();
        double totalPayout = sellOrderValue - orderFee;

        if(bankWallet.sufficientBalance(totalPayout)) {
            if (clientWallet.sufficientAsset(asset, order.getAssetAmount())) {
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
        clientWallet.addToBalance(totalPayout);
        clientWallet.removeFromAsset(asset, order.getAssetAmount());

        bankWallet.removeFromBalance(totalPayout);
        bankWallet.removeFromAsset(asset, order.getAssetAmount());

        Transaction transaction = new Transaction(asset, sellOrderValue, order.getAssetAmount(), LocalDateTime.now(), orderFee, bankWallet, clientWallet);
        rootRepository.saveTransaction(transaction);
    }

    // Limit_Buy

    /**
     * Checks if the Limit_Buy order can be done, if yes -> save LimitBuyOrder in database
     * @param order
     * @return | author = Vanessa Philips
     */
    public String checkLbuyOrder(OrderDTO order) {
        double totalPrice = order.getLimit() * order.getAssetAmount();
        double orderFee = totalPrice * BigBangkApplicatie.bigBangk.getFeePercentage();
        double totalCost = totalPrice + (orderFee/2.0);

        if (clientWallet.sufficientBalance(totalCost)) {
            Limit_Buy limit_buy = new Limit_Buy(asset, order.getLimit(), order.getAssetAmount(), LocalDateTime.now(), clientWallet);
            rootRepository.saveLimitBuyOrder(limit_buy);
        } else {
            return Messages.FundClient.getBody();
        }
        return Messages.WaitingLimitBuy.getBody();
    }

    // Limit_Sell

    /**
     * Checks if the Limit_Sell order can be done, if yes -> save LimitSellOrder in database
     * @param order
     * @return | author = Vanessa Philips
     */
    public String checkLsellOrder(OrderDTO order){
        if (clientWallet.sufficientAsset(asset, order.getAssetAmount())) {
            Limit_Sell limit_sell = new Limit_Sell(asset, order.getLimit(), order.getAssetAmount(), LocalDateTime.now(), clientWallet);
            rootRepository.saveLimitSellOrder(limit_sell);
        } else {
            return Messages.AssetClient.getBody();
        }
        return Messages.WaitingLimitSell.getBody();
    }

    // Stoploss_Sell

    /**
     * Checks if the Stoploss_Sell order can be done, if yes -> save StoplossSellOrder in database
     * @param order
     * @return | author = Vanessa Philips
     */
    public String checkSlossOrder(OrderDTO order){
        if (clientWallet.sufficientAsset(asset, order.getAssetAmount())) {
            Stoploss_Sell stoploss_sell = new Stoploss_Sell(asset, order.getLimit(), order.getAssetAmount(), LocalDateTime.now(), clientWallet);
            rootRepository.saveStoploss_Sell(stoploss_sell);
        } else {
            return Messages.AssetClient.getBody();
        }
        return Messages.WaitingStoplossSell.getBody();
    }

}

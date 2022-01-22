// Created by vip
// Creation date 02/12/2021

package com.example.project_bigbangk.model;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Orders.Limit_Buy;
import com.example.project_bigbangk.model.Orders.Limit_Sell;
import com.example.project_bigbangk.model.Orders.Stoploss_Sell;
import com.example.project_bigbangk.model.Orders.Transaction;

import java.util.*;

public class Wallet implements Cloneable {

    private String bank;
    private String iban;
    private double balance;
    private Map<Asset, Double> assets = new HashMap<>();
    private List<Transaction> transaction = new ArrayList<>();
    private List<Limit_Sell> limitSell = new ArrayList<>();
    private List<Limit_Buy> limitBuy = new ArrayList<>();
    private List<Stoploss_Sell> stoplossSell = new ArrayList<>();
    private ICanTrade owner;

    public Wallet(String bank, String iban, double balance, Map<Asset, Double> assets, List<Transaction> transaction,
                  List<Limit_Sell> limitSell, List<Limit_Buy> limitBuy, List<Stoploss_Sell> stoplossSell,
                  ICanTrade owner) {
        this.bank = bank;
        this.iban = iban;
        this.balance = balance;
        this.assets = assets;
        this.transaction = transaction;
        this.limitSell = limitSell;
        this.limitBuy = limitBuy;
        this.stoplossSell = stoplossSell;
        this.owner = owner;
    }

    public Wallet(String iban, double balance, Map<Asset, Double> assets) {
        this.iban = iban;
        this.balance = balance;
        this.assets = assets;
    }

    public Wallet(String iban, double balance) {
        this.iban = iban;
        this.balance = balance;
    }

    public void addToBalance(double addedCash){
        this.balance+= addedCash;
    }

    public void removeFromBalance(double removedCash){
        this.balance-= removedCash;
    }

    public void addToAsset(Asset asset, double addedAmount){
        this.assets.replace(asset, (this.assets.get(asset) + addedAmount));
    }

    public void removeFromAsset(Asset asset, double removedAmount){
        this.assets.replace(asset, (this.assets.get(asset) - removedAmount));
    }

    /**
     * Calculates the balance thats still free for the client to spend.
     * If a client has open buy orders he cannot make other buyorders using the same money. That's why we need to know the amount of the balance thats actually free for use in new orders.
     * @return double free balance
     */
    public double freeBalance(){
        double reservedBalance = 0.0;
        for (Limit_Buy limit_buy : limitBuy) {
            reservedBalance += limit_buy.getOrderLimit();
            reservedBalance += (limit_buy.getOrderLimit() * BigBangkApplicatie.bigBangk.getFeePercentage());
        }
        return balance - reservedBalance;
    }

    /**
     * similar to freebalance, if you have an open order to sell an asset it reserves those assets.
     * For clarity: if you have 100 BTC and an open sell order for 50BTC, you can make another sell order for 50, but not for 100.
     * @param asset the asset you want to check the amount of
     * @return amount of assets that aren't reserved for other orders.
     */
    public double freeAssetAmount(Asset asset){
        double reservedAssetAmount = 0.0;
        for (Limit_Sell limit_sell : limitSell) {
            if(limit_sell.getAsset() == asset){
                reservedAssetAmount += limit_sell.getAssetAmount();
            }
        }
        for (Stoploss_Sell stoploss_sell : stoplossSell) {
            if(stoploss_sell.getAsset() == asset){
                reservedAssetAmount += stoploss_sell.getAssetAmount();
            }
        }
        return assets.get(asset) - reservedAssetAmount;
    }

    /**
     * checks if the wallet has enough free balance against the amount you need.
     * @param amountNeeded the amount you need
     * @return boolean
     */
    public boolean sufficientBalance(double amountNeeded){
        return this.freeBalance() >= amountNeeded;
    }

    /**
     * checks if a wallet has enough free 'amount of an asset'
     * @param asset the asset to check the amount of
     * @param assetAmountNeeded the amount you want to check
     * @return boolean
     */
    public boolean sufficientAsset(Asset asset, double assetAmountNeeded){
        return this.freeAssetAmount(asset) >= assetAmountNeeded;
    }


    @Override
    public String toString() {
        return "Wallet{" +
                "bank='" + bank + '\'' +
                ", iban='" + iban + '\'' +
                ", balance=" + balance +
                ", assets=" + assets +
                ", transactions=" + transaction +
                ", limitSell=" + limitSell +
                ", limitBuy=" + limitBuy +
                ", stoplossSell=" + stoplossSell +
                ", owner=" + owner +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return Double.compare(wallet.balance, balance) == 0 && iban.equals(wallet.iban);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iban, balance);
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Map<Asset, Double> getAssets() {
        return assets;
    }

    public void setAssets(Map<Asset, Double> assets) {
        this.assets = assets;
    }

    public List<Transaction> getTransaction() {
        return transaction;
    }

    public void setTransaction(List<Transaction> transaction) {
        this.transaction = transaction;
    }

    public List<Limit_Sell> getLimitSell() {
        return limitSell;
    }

    public void setLimitSell(List<Limit_Sell> limitSell) {
        this.limitSell = limitSell;
    }

    public List<Limit_Buy> getLimitBuy() {
        return limitBuy;
    }

    public void setLimitBuy(List<Limit_Buy> limitBuy) {
        this.limitBuy = limitBuy;
    }

    public List<Stoploss_Sell> getStoplossSell() {
        return stoplossSell;
    }

    public void setStoplossSell(List<Stoploss_Sell> stoplossSell) {
        this.stoplossSell = stoplossSell;
    }

    public ICanTrade getOwner() {
        return owner;
    }

    public void setOwner(ICanTrade owner) {
        this.owner = owner;
    }

    @Override
    public Wallet clone(){
        try {
            return (Wallet)super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
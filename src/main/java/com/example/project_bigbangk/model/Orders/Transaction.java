package com.example.project_bigbangk.model.Orders;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

/**
 * Model created by Vanessa Philips.
 * Object "Transaction" > for "regular" transactions.
 */

public class Transaction{

    private final Logger logger = LoggerFactory.getLogger(Transaction.class);

    private Asset asset;
    private double priceExcludingFee;
    private double assetAmount;
    private LocalDateTime date;
    private double fee;
    private Wallet buyerWallet;
    private Wallet sellerWallet;
    private long orderId;

    public Transaction(long orderId, double priceExcludingFee, double assetAmount,
                       LocalDateTime date, double fee) {
        this.orderId = orderId;
        this.priceExcludingFee = priceExcludingFee;
        this.fee = fee;
        this.date = date;
        this.assetAmount = assetAmount;
        logger.info("New transaction, without Asset and Wallets");
    }

    public Transaction(Asset asset, double priceExcludingFee, double assetAmount, LocalDateTime date,
                       double fee, Wallet buyerWallet, Wallet sellerWallet) {
        this.asset = asset;
        this.priceExcludingFee = priceExcludingFee;
        this.date = date;
        this.assetAmount = assetAmount;
        this.fee = fee;
        this.buyerWallet = buyerWallet;
        this.sellerWallet = sellerWallet;
        logger.info("New transaction, without id");
    }

    public Logger getLogger() {
        return logger;
    }


    public Asset getAsset() {
        return asset;
    }


    public void setAsset(Asset asset) {
        this.asset = asset;
    }


    public double getPriceExcludingFee() {
        return priceExcludingFee;
    }


    public void setPriceExcludingFee(double priceExcludingFee) {
        this.priceExcludingFee = priceExcludingFee;
    }


    public double getAssetAmount() {
        return assetAmount;
    }

    public void setAssetAmount(double assetAmount) {
        this.assetAmount = assetAmount;
    }

    public LocalDateTime getDate() {
        return date;
    }


    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public Wallet getBuyerWallet() {
        return buyerWallet;
    }

    public void setBuyerWallet(Wallet buyerWallet) {
        this.buyerWallet = buyerWallet;
    }

    public Wallet getSellerWallet() {
        return sellerWallet;
    }

    public void setSellerWallet(Wallet sellerWallet) {
        this.sellerWallet = sellerWallet;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "asset=" + asset +
                ", priceExcludingFee=" + priceExcludingFee +
                ", assetAmount=" + assetAmount +
                ", date=" + date +
                ", fee=" + fee +
                ", buyerWallet=" + getBuyerWallet().getIban() +
                ", sellerWallet=" + getSellerWallet().getIban() +
                ", orderId=" + orderId +
                '}';
    }
}

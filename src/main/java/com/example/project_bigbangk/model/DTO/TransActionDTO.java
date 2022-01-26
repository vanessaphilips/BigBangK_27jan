// Created by Deek
// Creation date 1/26/2022

package com.example.project_bigbangk.model.DTO;

import com.example.project_bigbangk.model.Asset;

import java.time.LocalDateTime;

public class TransActionDTO {


    private Asset asset;
    private double priceExcludingFee;
    private double assetAmount;
    private LocalDateTime dateTime;
    private double fee;
    private WalletOwner seller;

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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public double getFee() {
        return fee;
    }

    public void setFee(double fee) {
        this.fee = fee;
    }

    public WalletOwner getSeller() {
        return seller;
    }

    public void setSeller(WalletOwner seller) {
        this.seller = seller;
    }

    public WalletOwner getBuyer() {
        return buyer;
    }

    public void setBuyer(WalletOwner buyer) {
        this.buyer = buyer;
    }

    private WalletOwner buyer;


    public TransActionDTO(Asset asset, double priceExcludingFee, double assetAmount, LocalDateTime dateTime, double fee, WalletOwner seller, WalletOwner buyer) {
        super();
        this.asset = asset;
        this.priceExcludingFee = priceExcludingFee;
        this.assetAmount = assetAmount;
        this.dateTime = dateTime;
        this.fee = fee;
        this.seller = seller;
        this.buyer = buyer;
    }
}
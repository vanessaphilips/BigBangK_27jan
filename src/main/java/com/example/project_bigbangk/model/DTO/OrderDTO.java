package com.example.project_bigbangk.model.DTO;
/*

@Author Philip Beeltje, Studentnummer: 500519452
*/

import com.example.project_bigbangk.model.Asset;

import java.time.LocalDateTime;

public class OrderDTO {


    private String assetCode; //automatisch ingevuld veld.
    private String orderType; //dropdown menu of zoiets.
    private double limit; //de trigger voor alle orders behalve transacties met bank. Sprint 3 default 0 of -1 bij transacties? (BUY SELL als type)
    private double assetAmount; //hoeveelheid coin of hoeveelheid geld(alleen in het geval dat je van de bank koopt. dan koop je 100€ aan bitcoin ipv 0.0004 bitcoin.

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public WalletOwner getWalletOwner() {
        return walletOwner;
    }

    public void setWalletOwner(WalletOwner walletOwner) {
        this.walletOwner = walletOwner;
    }

    private Asset asset;
    private WalletOwner walletOwner;
    private int orderID;
    private LocalDateTime dateTime;

    public OrderDTO(String assetCode, String orderType, double limit, double assetAmount) {
        this.assetCode = assetCode;
        this.orderType = orderType;
        this.limit = limit;
        this.assetAmount = assetAmount;
    }

    OrderDTO() {

    }

    public OrderDTO(String assetCode, String orderType, double limit, double assetAmount, int orderId, WalletOwner walletOwner, LocalDateTime dateTime, Asset asset) {
        this(assetCode, orderType, limit, assetAmount);
        this.orderID = orderId;
        this.walletOwner = walletOwner;
        this.dateTime = dateTime;
        this.asset = asset;
    }


    public int getOrderID() {
        return orderID;
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getAssetCode() {
        return assetCode;
    }

    public void setAssetCode(String assetCode) {
        this.assetCode = assetCode;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public double getLimit() {
        return limit;
    }

    public void setLimit(double limit) {
        this.limit = limit;
    }

    public double getAssetAmount() {
        return assetAmount;
    }

    public void setAssetAmount(double assetAmount) {
        this.assetAmount = assetAmount;
    }


}

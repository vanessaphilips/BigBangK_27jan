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
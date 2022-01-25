package com.example.project_bigbangk.model.DTO;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Wallet;

public class GraphicsWalletDTO {
    public Wallet nieuw;
    public String dateTime;

    public GraphicsWalletDTO(Wallet nieuw, String dateTime) {
        this.nieuw = nieuw;
        this.dateTime = dateTime;
    }

    /*public Asset asset; // asset naam, asset code, en prijs van dat moment/aankoopprijs?
    public String dateTime; // tijd en datum van deze bundel gegevens
    public double aantalVanAsset; // dat in de wallet zit op dit gegeven moment
    public double balance; // aantal euro's op de bank
    public double aankoopPrijs;

    public GraphicsWalletDTO(Asset asset, String dateTime, double aantalVanAsset) {
        this.asset = asset;
        this.dateTime = dateTime;
        this.aantalVanAsset = aantalVanAsset;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Asset getAsset() {
        return asset;
    }

    public String getDateTime() {
        return dateTime;
    }

    public double getAantalVanAsset() {
        return aantalVanAsset;
    }

    public double getAankoopPrijs() {
        return aankoopPrijs;
    }*/
}

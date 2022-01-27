package com.example.project_bigbangk.model.DTO;
/*

@Author Philip Beeltje, Studentnummer: 500519452
*/

import com.example.project_bigbangk.model.Asset;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WalletDTO {

    private String iban;
    private double balance;
    private Map<Asset, Double> assets;
    private double totalWorth;
    private double freeBalance;

    public WalletDTO(String iban, double balance, Map<Asset, Double> assets, double totalWorth, double freeBalance) {
        this.iban = iban;
        this.balance = balance;
        this.assets = assets;
        this.totalWorth = totalWorth;
        this.freeBalance = freeBalance;
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

    public double getTotalWorth() {
        return totalWorth;
    }

    public void setTotalWorth(double totalWorth) {
        this.totalWorth = totalWorth;
    }

    public double getFreeBalance() {
        return freeBalance;
    }

    public void setFreeBalance(double freeBalance) {
        this.freeBalance = freeBalance;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalletDTO walletDTO = (WalletDTO) o;
        return iban.equals(walletDTO.iban);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iban);
    }
}


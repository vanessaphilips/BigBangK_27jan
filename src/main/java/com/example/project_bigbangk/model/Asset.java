package com.example.project_bigbangk.model;

import java.util.Objects;

/**
 * Created by Karim Ajour on 3-12-2021 for Project_Big_Bangk
 */

public class Asset {

    private String code;
    private String name;
    private double currentPrice;

    public Asset( String code,String name, double currentPrice) {
        this.currentPrice = currentPrice;
        this.name = name;
        this.code = code;

    }

    public Asset( String code, String name) {
        this.name = name;
        this.code = code;
    }


    @Override
    public String toString() {
        return String.format("%s (%s)", code, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Asset asset = (Asset) o;
        return code.equals(asset.code) && name.equals(asset.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }
}

package com.example.project_bigbangk.model.DTO;

import java.util.Objects;

/**
 * Class containing an cryptocoin with its code, name and currentPrice
 * @author  Pieter Jan
 */

public class AssetDTO {

    private String code;
    private String name;
    private double currentPrice;

    public AssetDTO() {
    }

    public AssetDTO(String code, String name) {
        this.name = name;
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetDTO assetDTO = (AssetDTO) o;
        return code.equals(assetDTO.code) && name.equals(assetDTO.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, name);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", code, name);
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

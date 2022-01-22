// Created by Deek
//

package com.example.project_bigbangk.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A collection of priceDates in chronological order for a specific Asset
 * @author Pieter Jan Bleichrodt
 * Creation date 12/12/2021
 */
public class PriceHistory {


    private Asset asset;

    public List<PriceDate> getPriceDates() {
        return priceDates;
    }

    public void setPriceDates(List<PriceDate> priceDates) {
        this.priceDates = priceDates;
    }

    List<PriceDate> priceDates;

    public PriceHistory(List<PriceDate> priceDates, Asset asset) {
        this.priceDates = priceDates;
        Collections.sort(this.priceDates);
        this.asset = asset;
    }

    public PriceHistory(PriceDate priceDate) {
        priceDates = new ArrayList<>();
        this.priceDates.add(priceDate);
    }

    public Asset getAsset() {
        return asset;
    }


    public void setAsset(Asset asset) {
        this.asset = asset;
    }


}
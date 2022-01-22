
package com.example.project_bigbangk.model.DTO;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.PriceDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * for sending the priceHistopry data to the client.
 * @author Pieter jan Bleichrodt
 */
public class PriceHistoryDTO {

    List<PriceDateDTO> priceDates;

    public List<PriceDateDTO> getPriceDates() {
        return priceDates;
    }

    private Asset asset;

    public Asset getAsset() {
        return asset;
    }

    public void setAsset(Asset asset) {
        this.asset = asset;
    }

    public PriceHistoryDTO(List<PriceDateDTO> priceDates, Asset asset) {
        super();
        this.priceDates = priceDates;
        this.asset = asset;
    }


}
// Created by Deek
// Creation date 12/31/2021

package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.PriceDate;
import com.example.project_bigbangk.model.PriceHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * for caching the PriceHistory data and thus balancing the load on the database.
 * @author Pieter Jan Bleichrodt
 */
@Repository
public class PriceHistoryCache {

    List<PriceHistory> priceHistories;
    private final Logger logger = LoggerFactory.getLogger(PriceHistoryCache.class);

    public PriceHistoryCache() {
        super();
        logger.info("New PriceHistoryCache");
        this.priceHistories = new ArrayList<>();
    }

    /**
     * retrieves the pricehistories for all Assets from the database
     * @param localDateTime date in past for defining the interval for which priceHistoryData is retrieved
     * @return a list of PriceHistory
     */
    public List<PriceHistory> getPriceHistoriesFromDate(LocalDateTime localDateTime) {
        List<PriceHistory> returnPriceHistories= new ArrayList<>();
        for(PriceHistory priceHistory: priceHistories){
            List<PriceDate> priceDates = priceHistory.getPriceDates().stream().filter(d->d.getDateTime().isAfter(localDateTime)).collect(Collectors.toList());
            returnPriceHistories.add(new PriceHistory(priceDates, priceHistory.getAsset()));
        }
        return returnPriceHistories;
    }


    public void setPriceHistories(List<PriceHistory> priceHistories) {
        this.priceHistories = priceHistories;
    }



}
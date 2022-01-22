// Created by Deek
// Creation date 12/20/2021

package com.example.project_bigbangk.service;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.DTO.PriceDateDTO;
import com.example.project_bigbangk.model.DTO.PriceHistoryDTO;
import com.example.project_bigbangk.model.PriceDate;
import com.example.project_bigbangk.model.PriceHistory;
import com.example.project_bigbangk.repository.PriceHistoryCache;
import com.example.project_bigbangk.repository.RootRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class MarketPlaceService {

    private final Logger logger = LoggerFactory.getLogger(MarketPlaceService.class);
    private final RootRepository rootRepository;
    PriceHistoryCache priceHistoryCache;

    public MarketPlaceService(RootRepository rootRepository, PriceHistoryCache priceHistoryCache) {
        super();
        this.rootRepository = rootRepository;
        logger.info("New MarketPlaceService");
        this.priceHistoryCache = priceHistoryCache;
    }

    public List<Asset> getAllAssets() {
        return rootRepository.getAllAssets();
    }

    public List<PriceHistoryDTO> getAllPriceHistoriesFromDate(LocalDateTime localDateTime) {
        List<PriceHistory> priceHistories;
        if (LocalDateTime.now().minusDays(BigBangkApplicatie.DAYS_OF_PRICEHISTORY_CACHE).isBefore(localDateTime)) {
            priceHistories = priceHistoryCache.getPriceHistoriesFromDate(localDateTime);
        } else {
            priceHistories = rootRepository.getAllPriceHistories(localDateTime);
        }
        if(priceHistories.isEmpty()){
            priceHistories = rootRepository.getAllPriceHistories(localDateTime);
        }
        return convertPriceHistroyToDTO(priceHistories);
    }

    private List<PriceHistoryDTO> convertPriceHistroyToDTO(List<PriceHistory> priceHistories) {
        List<PriceHistoryDTO> priceHistoriesDTO = new ArrayList<>();
               for (PriceHistory priceHistory : priceHistories) {
            List<PriceDateDTO> priceDateDTOS = new ArrayList<>();
            for (PriceDate priceDate : priceHistory.getPriceDates()) {
                priceDateDTOS.add(new PriceDateDTO(priceDate.getDateTime().toString(), priceDate.getPrice()));
            }
            priceHistoriesDTO.add(new PriceHistoryDTO(priceDateDTOS, priceHistory.getAsset()));
        }

        return priceHistoriesDTO;
    }
}
// Created by Deek
// Creation date 12/31/2021

package com.example.project_bigbangk.service.priceHistoryUpdate;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.repository.PriceHistoryCache;
import com.example.project_bigbangk.repository.RootRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * is notified when a priceHistoryUpdate occurrences and updates the PriceHistoryCache
 * @author Pieter jan Bleichrodt
 */
@Service
public class PriceHistoryCacheUpdateService implements IObserver {


    private final Logger logger = LoggerFactory.getLogger(PriceHistoryCacheUpdateService.class);
    RootRepository rootRepository;
    PriceHistoryCache priceHistoryCache;
    ISubject subject;

    public PriceHistoryCacheUpdateService(RootRepository rootRepository, PriceHistoryCache priceHistoryCache, ISubject subject) {
        super();
        logger.info("New PriceHistoryCacheUpdateService");
        this.rootRepository = rootRepository;
        this.priceHistoryCache = priceHistoryCache;
        this.subject = subject;
        subject.addListener(this);
    }

    @Override
    public void update() {
        priceHistoryCache.setPriceHistories(
                rootRepository.getAllPriceHistories(LocalDateTime.now().minusDays(BigBangkApplicatie.DAYS_OF_PRICEHISTORY_CACHE))
        );
        logger.info("PriceHistoryCache Updated");
    }
}
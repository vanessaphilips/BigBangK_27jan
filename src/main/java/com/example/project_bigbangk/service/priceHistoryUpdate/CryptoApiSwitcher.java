// Created by Deek
// Creation date 12/11/2021

package com.example.project_bigbangk.service.priceHistoryUpdate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 *
 * This class selects the CryptoApiNegotiatorService that is online and injects it in the
 * PriceHistoryUpdateService
 */
@Service
public class CryptoApiSwitcher implements ICryptoApiSwitcher {

    private final Logger logger = LoggerFactory.getLogger(CryptoApiSwitcher.class);
    private List<ICryptoApiNegotiator> cryptoApiNegotiators;


    public CryptoApiSwitcher() {
        super();
        logger.info("New CryptoApiSwitcher");
        this.cryptoApiNegotiators = new ArrayList<>();
    }

    /**
     * selects which Api server is online
     * @return concrete implementation of ICryptoApiNegotiator
     */
    @Override
    public ICryptoApiNegotiator getAvailableNegotiator() {
       return cryptoApiNegotiators.stream().filter(ICryptoApiNegotiator::isAvailable).findFirst().orElse(null);
    }
    /**
     * for adding additional concrete implementation of ICryptoApiNegotiator
     */
    @Override
    public void addNegotiator(ICryptoApiNegotiator coinMarketCapNegotiator) {
        cryptoApiNegotiators.add(coinMarketCapNegotiator);
    }
}
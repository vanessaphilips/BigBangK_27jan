package com.example.project_bigbangk.model.Orders;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

/**
 * Model created by Vanessa Philips.
 * Object "Limit_Buy" > Buy transaction if the price reaches below a desired level.
 */

public class Limit_Buy extends AbstractOrder {

    private final Logger logger = LoggerFactory.getLogger(Limit_Buy.class);

    private Wallet buyer;

    public Limit_Buy(int orderId, double orderLimit, double assetAmount, LocalDateTime date) {
        super(orderId, orderLimit, assetAmount, date);
        logger.info("New Limit_Buy, without Asset and Wallet");
    }

    public Limit_Buy(Asset asset, double orderLimit, double assetAmount, LocalDateTime date, Wallet buyer) {
        super(asset, orderLimit, assetAmount, date);
        this.buyer = buyer;
        logger.info("New Limit_Buy, without id");
    }

    public Wallet getBuyer() {
        return buyer;
    }

    public void setBuyer(Wallet buyer) {
        this.buyer = buyer;
    }


    @Override
    public String toString() {
        return super.toString() +
                ", buyerWallet=" + getBuyer().getIban();
    }


}
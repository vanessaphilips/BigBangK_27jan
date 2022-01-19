package com.example.project_bigbangk.model.Orders;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;

/**
 * Model created by Vanessa Philips.
 * Object "Stoploss_Sell" > Sell (trigger)transaction if price reaches a certain level below the current price.
 */

public class Stoploss_Sell extends AbstractOrder{

    private final Logger logger = LoggerFactory.getLogger(Stoploss_Sell.class);

    private Wallet seller;


    public Stoploss_Sell(int orderId, double orderLimit, double assetAmount, LocalDateTime date) {
        super(orderId, orderLimit, assetAmount, date);
        logger.info("New Stoploss_Sell, without Asset and Wallet");
    }

    public Stoploss_Sell(Asset asset, double orderLimit, double assetAmount, LocalDateTime date, Wallet seller) {
        super(asset, orderLimit, assetAmount, date);
        this.seller = seller;

        logger.info("New Stoploss_Sell, without id");
    }

    public Wallet getSeller() {
        return seller;
    }

    public void setSeller(Wallet seller) {
        this.seller = seller;
    }


    @Override
    public String toString() {
        return super.toString() +
                "Stoploss_Sell{" +
                "sellerWallet=" + seller +
                '}';
    }
}
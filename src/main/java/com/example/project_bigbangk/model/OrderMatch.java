// Created by Deek
// Creation date 1/25/2022

package com.example.project_bigbangk.model;

import com.example.project_bigbangk.model.Orders.AbstractOrder;
import com.example.project_bigbangk.model.Orders.Limit_Buy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderMatch {

    private final Logger logger = LoggerFactory.getLogger(OrderMatch.class);


    private Limit_Buy limit_buy;
    private AbstractOrder limitSell;

    public OrderMatch() {
        super();
        logger.info("New OrderMatch");
    }

    public Limit_Buy getLimit_buy() {
        return limit_buy;
    }

    public void setLimit_buy(Limit_Buy limit_buy) {
        this.limit_buy = limit_buy;
    }

    public AbstractOrder getLimitSell() {
        return limitSell;
    }

    public void setLimitSell(AbstractOrder limitSell) {
        this.limitSell = limitSell;
    }

}
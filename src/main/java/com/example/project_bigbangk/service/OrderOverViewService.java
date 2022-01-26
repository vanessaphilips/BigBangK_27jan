// Created by Deek
// Creation date 1/26/2022

package com.example.project_bigbangk.service;

import com.example.project_bigbangk.model.Orders.Limit_Buy;
import com.example.project_bigbangk.model.Orders.Limit_Sell;
import com.example.project_bigbangk.model.Orders.Stoploss_Sell;
import com.example.project_bigbangk.model.Orders.Transaction;
import com.example.project_bigbangk.repository.RootRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class OrderOverViewService {

    private final Logger logger = LoggerFactory.getLogger(OrderOverViewService.class);
    private RootRepository rootRepository;

    public OrderOverViewService(RootRepository rootRepository) {
        super();
        this.rootRepository = rootRepository;
        logger.info("New OrderOverViewService");
    }

    public List<Limit_Buy> getAllLimitBuy(){
       return  rootRepository.getAllLimitBuys();
    }
    public List<Limit_Sell> getAllLimitSell(){
        return rootRepository.getAllLimitSells();
    }
    public List<Stoploss_Sell> getAllStopLossSells(){
        return rootRepository.getAllStopLossSells();
    }

}
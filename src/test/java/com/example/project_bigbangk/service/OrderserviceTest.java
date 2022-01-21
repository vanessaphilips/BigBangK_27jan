//package com.example.project_bigbangk.service;
//
//import com.example.project_bigbangk.model.Asset;
//import com.example.project_bigbangk.model.AssetCode_Name;
//import com.example.project_bigbangk.model.Client;
//import com.example.project_bigbangk.model.DTO.OrderDTO;
//import com.example.project_bigbangk.model.Orders.Limit_Buy;
//import com.example.project_bigbangk.model.Orders.Limit_Sell;
//import com.example.project_bigbangk.model.Wallet;
//import com.example.project_bigbangk.repository.RootRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import javax.annotation.Resource;
//
//import java.time.LocalDateTime;
//
//import static org.assertj.core.api.Assertions.fail;
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Created by Vanessa Philips, 20/01/22
// */
//@SpringBootTest
//class OrderserviceTest {
//
//    @Resource
//    Orderservice orderservice;
//
//    @Resource
//    RootRepository rootRepository;
//
//    @BeforeEach
//    void setUp() {
//        // String assetCode, String orderType, double limit, double assetAmount
//    }
//
//    @Test
//    void handleOrderByType() {
//        orderservice.handleOrderByType();
//    }
//
//    @Test
//    void checkBuyOrder() {
//        orderservice.checkBuyOrder();
//    }
//
//    @Test
//    void checkSellOrder() {
//        orderservice.checkSellOrder();
//    }
//
//    @Test
//    void checkLbuyOrder() {
//        orderservice.checkLbuyOrder();
//    }
//
//    @Test
//    void checkLsellOrder() {
//        orderservice.checkLsellOrder();
//    }
//
//    @Test
//    void checkSlossOrder() {
//        orderservice.checkSlossOrder();
//    }
//}
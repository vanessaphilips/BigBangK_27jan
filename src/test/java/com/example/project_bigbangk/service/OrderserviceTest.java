//package com.example.project_bigbangk.service;
//
//import com.example.project_bigbangk.model.Asset;
//import com.example.project_bigbangk.model.AssetCode_Name;
//import com.example.project_bigbangk.model.Client;
//import com.example.project_bigbangk.model.DTO.OrderDTO;
//import com.example.project_bigbangk.model.DTO.RegistrationDTO;
//import com.example.project_bigbangk.model.Orders.Limit_Buy;
//import com.example.project_bigbangk.model.Orders.Limit_Sell;
//import com.example.project_bigbangk.model.Wallet;
//import com.example.project_bigbangk.repository.RootRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.Mockito;
//import org.springframework.boot.test.context.SpringBootTest;
//import javax.annotation.Resource;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import static org.assertj.core.api.Assertions.fail;
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * OrderserviceTest created by Vanessa Philips, 20/01/22
// */
//
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
//        // OrderDTO = String assetCode, String orderType, double limit, double assetAmount
//
//    }
//
//    @Test
//    void handleOrderByType() {
//        OrderDTO orderDTOBuy = new OrderDTO("VIP", "BUY", 1,500);
//        OrderDTO orderDTOSell = new OrderDTO("CAT", "SELL", 0.5, 750);
//        OrderDTO orderDTOLimit_Buy = new OrderDTO("DOG", "LIMIT_BUY", 0.75, 600);
//        OrderDTO orderDTOLimit_Sell = new OrderDTO("FLY", "LIMIT_SELL", 1, 550);
//        OrderDTO orderDTOStoploss_Sell = new OrderDTO("ICE", "STOPLOSS_SELL", 0.8, 450);
//        Client clientFromToken = new Client("client@test.nl", "Client", null, "Test", LocalDate.of(1999, 9, 9), "123456789", "clienttest", null, null);
//        orderservice.handleOrderByType(orderDTOBuy, clientFromToken);
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
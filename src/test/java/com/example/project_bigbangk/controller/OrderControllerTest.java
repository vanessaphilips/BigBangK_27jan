package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.Utilities.ObjectToJsonHelper;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.LoginDTO;
import com.example.project_bigbangk.model.DTO.OrderDTO;
import com.example.project_bigbangk.model.DTO.RegistrationDTO;
import com.example.project_bigbangk.service.LoginService;
import com.example.project_bigbangk.service.Orderservice;
import com.example.project_bigbangk.service.RegistrationService;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import com.example.project_bigbangk.service.WalletService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Vanessa Philips, 20/01/22
 */
@WebMvcTest
@TestPropertySource(properties =
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration")
class OrderControllerTest {
    private MockMvc mockMvc;
    @MockBean
    MarketPlaceController marketPlaceController;
    @MockBean
    WalletController walletController;
    @MockBean
    AuthenticateService authenticateService;
    @MockBean
    private Orderservice orderService;
    @MockBean
    private RegistrationService registrationService;
    @MockBean
    private LoginService loginService;
    @MockBean
    private WalletService walletService;
    MockHttpServletRequestBuilder builder;

    @Autowired
    public OrderControllerTest(MockMvc mockMvc) {
        super();
        this.mockMvc = mockMvc;
       OrderDTO orderDTO = new OrderDTO("VIP", "BUY", 500, 1500);
        this.builder = MockMvcRequestBuilders.post("/placeorder")
                .content(ObjectToJsonHelper.objectToJson(orderDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }
//
//    @Test
//    void orderFail() {
//        Mockito.when(orderService.handleOrderByType("BUY","vip@vip.com"));
//
//        // Van login test:
//        Mockito.when(loginService.login("deek", "password12345")).thenReturn(null);
//        ResultActions response;
//        try {
//            response = mockMvc.perform(builder);
//            MvcResult result = response.andExpect(MockMvcResultMatchers.status().isUnauthorized())
//                    .andReturn();
//            assertEquals("Username or password not valid", result.getResponse().getContentAsString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Test
//    void orderSucces() {
//        String message = "Buy-Order successful";
//        Mockito.when(orderService.checkBuyOrder());
//
//        // Van login test:
//        String message = "login succesfull";
//        Mockito.when(loginService.login("deek", "password12345")).thenReturn(message);
//        ResultActions response;
//
//        try {
//            response = mockMvc.perform(builder);
//            MvcResult result = response.andExpect(MockMvcResultMatchers.status().isOk())
//                    .andReturn();
//            assertEquals("{\"authorization\":\"login succesfull\"}", result.getResponse().getContentAsString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }


//        @Test
//        void placeOrder(){
//            fail("Gegenereerde test, nog te maken?");
//        }
    }
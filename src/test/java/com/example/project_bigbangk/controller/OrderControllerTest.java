package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.Utilities.ObjectToJsonHelper;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.LoginDTO;
import com.example.project_bigbangk.model.DTO.OrderDTO;
import com.example.project_bigbangk.model.DTO.RegistrationDTO;
import com.example.project_bigbangk.service.*;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import com.example.project_bigbangk.service.Security.HashService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    MarketPlaceController marketPlaceController; // gebruikt de test
    @MockBean
    WalletController walletController; // gebruikt de test
    @MockBean
    AuthenticateService authenticateService; // gebruikt de test
    @MockBean
    private RegistrationService registrationService; // gebruikt de test
    @MockBean
    private LoginService loginService; // gebruikt de test
    @MockBean
    private Orderservice orderService; // gebruikt de test
    MockHttpServletRequestBuilder builder; // gebruikt de test

    @Autowired
    public OrderControllerTest(MockMvc mockMvc) {
        super();
        this.mockMvc = mockMvc;
    }

    @Test
    void orderFail() {
        OrderDTO orderDTO = new OrderDTO("VIP", "BUY", 500, 1500);
        Client client = Mockito.mock(Client.class);
        this.builder = MockMvcRequestBuilders.post("/placeorder")
                .header("authorization", "token")
                .content(ObjectToJsonHelper.objectToJson(orderDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        Mockito.when(authenticateService.authenticate("token")).thenReturn(false);
        Mockito.when(orderService.handleOrderByType(orderDTO, client)).thenReturn(ResponseEntity.status(401).body("token expired"));
        try {
            mockMvc.perform(builder)
                    .andExpect(MockMvcResultMatchers.status().is(401));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void orderSucces() {
        OrderDTO orderDTO = new OrderDTO("VIP", "SELL", 250, 1250);
        Client client = Mockito.mock(Client.class);
        this.builder = MockMvcRequestBuilders.post("/placeorder")
                .header("authorization", "token")
                .content(ObjectToJsonHelper.objectToJson(orderDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
        Mockito.when(authenticateService.authenticate("token")).thenReturn(true);
        Mockito.when(orderService.handleOrderByType(orderDTO, client)).thenReturn(ResponseEntity.status(201).body("Sell order Successful"));
        try {
            mockMvc.perform(builder)
                    .andExpect(MockMvcResultMatchers.status().is(201));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

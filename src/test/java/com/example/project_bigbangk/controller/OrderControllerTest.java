package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.service.LoginService;
import com.example.project_bigbangk.service.Orderservice;
import com.example.project_bigbangk.service.RegistrationService;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

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
    Orderservice orderservice;
    @MockBean
    AuthenticateService authenticateService;
    @MockBean
    private RegistrationService registrationService;
    @MockBean
    private LoginService loginService;
    MockHttpServletRequestBuilder builder;

    @Autowired
    public OrderControllerTest(MockMvc mockMvc) {
        super();
        this.mockMvc = mockMvc;
    }

        @Test
        void placeOrder () throws Exception {
            fail("Nog te maken test");

        }
    }
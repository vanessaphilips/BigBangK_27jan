package com.example.project_bigbangk.controller;

import com.example.project_bigbangk.Utilities.ObjectToJsonHelper;
import com.example.project_bigbangk.model.DTO.RegistrationDTO;
import com.example.project_bigbangk.service.DeleteOrderService;
import com.example.project_bigbangk.service.LoginService;
import com.example.project_bigbangk.service.RegistrationService;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;


@WebMvcTest
@TestPropertySource(properties =
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration")
class RegistrationTest {
    @MockBean
    DeleteOrderController deleteOrderController;
    private MockMvc mockMvc;
    @MockBean
    OrderController orderController;
    @MockBean
    MarketPlaceController marketPlaceController;
    @MockBean
    LoginService loginService;
    @MockBean
    AuthenticateService authenticateService;
    @MockBean
    WalletController walletController;
    @MockBean
    RegistrationService registrationService;
    MockHttpServletRequestBuilder builder;

    @Autowired
    public RegistrationTest(MockMvc mockMvc){
        super();
        this.mockMvc = mockMvc;
    }

    @Test
    void RegisterClient() throws Exception {

        RegistrationDTO registrationDTO = new RegistrationDTO("henk@unicom.nl", "password1234345", "Henk", "de", "Kort",
                "123434546", "1950-01-01", "1111BN", "Straatie", 9, "Muiden", "NLD");

        Mockito.when(registrationService.registerClient(registrationDTO)).thenReturn("Registration Successful");

        builder = MockMvcRequestBuilders.post("http://localhost:8080/register")
                .content(ObjectToJsonHelper.objectToJson(registrationDTO))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().is(201));
    }
}
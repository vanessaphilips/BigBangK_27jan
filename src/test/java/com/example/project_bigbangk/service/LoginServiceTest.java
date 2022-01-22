package com.example.project_bigbangk.service;

import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.repository.RootRepository;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import com.example.project_bigbangk.service.Security.ITokenService;
import com.example.project_bigbangk.service.Security.JWTService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * created by Pieter Jan Bleichrodt
 * created 12/9/2021
 */
class LoginServiceTest {

    static ITokenService tokenService = Mockito.mock(JWTService.class);
    static RootRepository rootRepository = Mockito.mock(RootRepository.class);
    static AuthenticateService authenticateService = Mockito.mock(AuthenticateService.class);
    static Client client = Mockito.mock(Client.class);
    static LoginService loginService;
    static final String EMAIL = "deek@deek.nl";
    static final String PASSWORD = "DeekPW";

    @BeforeAll
    public static void setup() {
        Mockito.when(client.getFirstName()).thenReturn("Deek");
        Mockito.when(client.getEmail()).thenReturn("deek@deek.nl");
        Mockito.when(rootRepository.findClientByEmail(EMAIL)).thenReturn(client);
        Mockito.when(tokenService.getToken(EMAIL, client.getFirstName())).thenReturn("token");
        loginService = new LoginService(authenticateService, tokenService, rootRepository);
    }

    @Test
    public void loginSucces() {
        Mockito.when(authenticateService.authenticate(EMAIL, PASSWORD)).thenReturn(true);
        String expected = "Bearer token";
        String actual = loginService.login("deek@deek.nl", "DeekPW");
        assertEquals(expected, actual);
    }
    @Test
    public void loginUnSuccesFull() {
        Mockito.when(authenticateService.authenticate(EMAIL, PASSWORD)).thenReturn(false);
        String expected = null;
        String actual = loginService.login("deek@deek.nl", "DeekPW");
        assertEquals(expected, actual);
    }
    @Test
    public void loginUnSuccesFullWithNull() {
        String expected = null;
        String actual = loginService.login(null, null);
        assertEquals(expected, actual);
    }
}
package com.example.project_bigbangk.service;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.OrderDTO;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * OrderserviceTest created by Vanessa Philips, 20/01/22
 */

class OrderserviceTest {

    private Orderservice orderservice;
    private RootRepository rootRepository;

    OrderDTO orderDTOBuy = new OrderDTO("VIP", "BUY", 1,500);
    OrderDTO orderDTOSell = new OrderDTO("CAT", "SELL", 0.5, 750);
    OrderDTO orderDTOLimit_Buy = new OrderDTO("DOG", "LIMIT_BUY", 0.75, 600);
    OrderDTO orderDTOLimit_Sell = new OrderDTO("FLY", "LIMIT_SELL", 1, 550);
    OrderDTO orderDTOStoploss_Sell = new OrderDTO("ICE", "STOPLOSS_SELL", 0.8, 450);

    @BeforeEach
    void setUp() {
        rootRepository = Mockito.mock(RootRepository.class);
        orderservice = new Orderservice(rootRepository);
    }

    @Test
    void handleOrderByTypeOk() {
        Wallet bankWallet = createMockedWalletSufficient();
        Mockito.when(rootRepository.findWalletbyBankCode(Mockito.anyString())).thenReturn(bankWallet);
        Asset asset = Mockito.mock(Asset.class);
        Mockito.when(rootRepository.findAssetByCode(Mockito.anyString())).thenReturn(asset);
        Client client = Mockito.mock(Client.class);
        Wallet clientWallet = createMockedWalletSufficient();
        Mockito.when(client.getWallet()).thenReturn(clientWallet);

        ResponseEntity actual = orderservice.handleOrderByType(orderDTOBuy, client);
        ResponseEntity expected = ResponseEntity.status(201).body(Orderservice.Messages.SuccessBuy.getBody());
        assertEquals(expected,actual);

        actual = orderservice.handleOrderByType(orderDTOSell, client);
        expected = ResponseEntity.status(201).body(Orderservice.Messages.SuccessSell.getBody());
        assertEquals(expected, actual);

        actual = orderservice.handleOrderByType(orderDTOLimit_Buy, client);
        expected = ResponseEntity.status(201).body(Orderservice.Messages.WaitingLimitBuy.getBody());
        assertEquals(expected,actual);

        actual = orderservice.handleOrderByType(orderDTOLimit_Sell, client);
        expected = ResponseEntity.status(201).body(Orderservice.Messages.WaitingLimitSell.getBody());
        assertEquals(expected, actual);

        actual = orderservice.handleOrderByType(orderDTOStoploss_Sell, client);
        expected = ResponseEntity.status(201).body(Orderservice.Messages.WaitingStoplossSell.getBody());
        assertThat(expected).isEqualTo(actual);

        actual = orderservice.handleOrderByType(orderDTOStoploss_Sell, client);
        expected = ResponseEntity.status(400).body(Orderservice.Messages.AssetClient.getBody());
        assertNotEquals(expected, actual);
    }

    @Test
    void handleOrderByTypeNok() {
        Wallet bankWallet = createMockedWalletNotSufficient();
        Mockito.when(rootRepository.findWalletbyBankCode(Mockito.anyString())).thenReturn(bankWallet);
        Asset asset = Mockito.mock(Asset.class);
        Mockito.when(rootRepository.findAssetByCode(Mockito.anyString())).thenReturn(asset);
        Client client = Mockito.mock(Client.class);
        Wallet clientWallet = createMockedWalletNotSufficient();
        Mockito.when(client.getWallet()).thenReturn(clientWallet);

        ResponseEntity actual = orderservice.handleOrderByType(orderDTOBuy, client);
        ResponseEntity expected = ResponseEntity.status(400).body(Orderservice.Messages.FundClient.getBody());
        assertEquals(expected,actual);

        actual = orderservice.handleOrderByType(orderDTOSell, client);
        expected = ResponseEntity.status(400).body(Orderservice.Messages.AssetClient.getBody());
        assertEquals(expected, actual);

        actual = orderservice.handleOrderByType(orderDTOLimit_Buy, client);
        expected = ResponseEntity.status(400).body(Orderservice.Messages.FundClient.getBody());
        assertEquals(expected,actual);

        actual = orderservice.handleOrderByType(orderDTOLimit_Sell, client);
        expected = ResponseEntity.status(400).body(Orderservice.Messages.AssetClient.getBody());
        assertEquals(expected, actual);

        actual = orderservice.handleOrderByType(orderDTOStoploss_Sell, client);
        expected = ResponseEntity.status(400).body(Orderservice.Messages.AssetClient.getBody());
        assertThat(expected).isEqualTo(actual);

        actual = orderservice.handleOrderByType(orderDTOLimit_Sell, client);
        expected = ResponseEntity.status(201).body(Orderservice.Messages.WaitingLimitSell.getBody());
        assertNotEquals(expected, actual);
    }

        private Wallet createMockedWalletSufficient(){
        Wallet wallet = Mockito.mock(Wallet.class);
        Mockito.when(wallet.sufficientAsset(Mockito.any(Asset.class),Mockito.anyDouble())).thenReturn(true);
        Mockito.when(wallet.sufficientBalance(Mockito.anyDouble())).thenReturn(true);
        return wallet;
    }

    private Wallet createMockedWalletNotSufficient(){
        Wallet wallet = Mockito.mock(Wallet.class);
        Mockito.when(wallet.sufficientAsset(Mockito.any(Asset.class),Mockito.anyDouble())).thenReturn(false);
        Mockito.when(wallet.sufficientBalance(Mockito.anyDouble())).thenReturn(false);
        return wallet;
    }

}
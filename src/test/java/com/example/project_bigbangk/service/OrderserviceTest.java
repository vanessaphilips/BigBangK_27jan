package com.example.project_bigbangk.service;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by Vanessa Philips, 20/01/22
 */
class OrderserviceTest {

    static Orderservice orderservice;

    static RootRepository rootRepository = Mockito.mock(RootRepository.class);
    static Client client = Mockito.mock(Client.class);
    static Wallet wallet = Mockito.mock(Wallet.class);
    static Asset asset = Mockito.mock(Asset.class);

    @BeforeEach
    void setUp() {
        orderservice = new Orderservice(rootRepository);

    }

    @Test
    void handleOrderByType() {
        fail("Nog te maken test");
    }

    @Test
    void checkBuyOrder() {
        fail("Nog te maken test");
    }

    @Test
    void checkSellOrder() {
        fail("Nog te maken test");
    }

    @Test
    void checkLbuyOrder() {
        fail("Nog te maken test");
    }

    @Test
    void checkLsellOrder() {
        fail("Nog te maken test");
    }

    @Test
    void checkSlossOrder() {
        fail("Nog te maken test");
    }

}
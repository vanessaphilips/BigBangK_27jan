

package com.example.project_bigbangk.service;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.AssetCode_Name;
import com.example.project_bigbangk.model.Bank;
import com.example.project_bigbangk.model.Orders.AbstractOrder;
import com.example.project_bigbangk.model.Orders.Limit_Buy;
import com.example.project_bigbangk.model.Orders.Limit_Sell;
import com.example.project_bigbangk.model.Orders.Transaction;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;


import javax.annotation.Resource;
import javax.print.attribute.standard.PrinterLocation;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.*;

@SpringBootTest
//@DirtiesContext(classMode= DirtiesContext.ClassMode.)

class OrderMatchingServiceTest {
    @MockBean
    RootRepository rootRepository;

    @MockBean
    BigBangkApplicatie bigBangkApplicatie;

    @Resource
    OrderMatchingService orderMatchingService;

    @MockBean
    TransactionService transactionService;
    LocalDateTime now;

    Limit_Buy[] limit_buyList = new Limit_Buy[4];
    Limit_Sell[] limit_sellList = new Limit_Sell[7];


    @BeforeEach
    public void setupMocks() {
        now = LocalDateTime.now();
        Asset assetBTC = (createmockedAsset(AssetCode_Name.BTC, 40000));
        Asset assetADA = (createmockedAsset(AssetCode_Name.ADA, 1.07));
        limit_buyList[0] = (createMockedLBuy(assetBTC, 40000, 7, now.minusDays(30)));
        limit_buyList[1] = (createMockedLBuy(assetBTC, 40000, 1, now.minusDays(60)));
        limit_buyList[2] = (createMockedLBuy(assetBTC, 11000, 1, now.minusDays(60)));
        limit_buyList[3] = (createMockedLBuy(assetADA, 1.2, 11, now.minusDays(90)));
        limit_sellList[0] = (createMockedLSell(assetBTC, 38000, 2.0, now.minusDays(30)));
        limit_sellList[1] = (createMockedLSell(assetBTC, 44000, 2.0, now.minusDays(60)));
        limit_sellList[2] = (createMockedLSell(assetADA, 1.1, 10, now.minusDays(90)));
        limit_sellList[3] = (createMockedLSell(assetBTC, 15000, 1, now.minusDays(90)));
        limit_sellList[4] = (createMockedLSell(assetBTC, 15000, 2, now.minusDays(120)));
        limit_sellList[5] = (createMockedLSell(assetBTC, 20000, 1, now.minusDays(30)));
        limit_sellList[6] = (createMockedLSell(assetBTC, 38000, 1.5, now.minusDays(0)));
        Mockito.when(transactionService.validateTransAction(Mockito.any(Transaction.class))).thenReturn(true);
        Mockito.when(rootRepository.getAllLimitBuy()).thenReturn(new ArrayList<>(List.of(limit_buyList)));
        Mockito.when(rootRepository.getAllLimitSell()).thenReturn(new ArrayList<>(List.of(limit_sellList)));
        Bank bigBangk = Mockito.mock(Bank.class);
        Mockito.when(bigBangk.getWallet()).thenReturn(Mockito.mock(Wallet.class));
        Mockito.when(bigBangk.getFeePercentage()).thenReturn(0.25);
        Mockito.when(bigBangkApplicatie.getBank()).thenReturn(bigBangk);

    }

    private Asset createmockedAsset(AssetCode_Name assetCodeName, double currentprice) {
        Asset asset = Mockito.when(Mockito.mock(Asset.class).getCurrentPrice()).thenReturn(currentprice).getMock();
        Mockito.when(asset.getCode()).thenReturn(assetCodeName.getAssetCode());
        Mockito.when(asset.getName()).thenReturn(assetCodeName.getAssetName());
        return asset;
    }

    private Limit_Buy createMockedLBuy(Asset asset, double orderLimit, double assetAmount, LocalDateTime ldt) {
        Limit_Buy limit_buy = Mockito.mock(Limit_Buy.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(limit_buy.getAssetAmount()).thenCallRealMethod();
        Mockito.doCallRealMethod().when(limit_buy).setAssetAmount(Mockito.anyDouble());
        limit_buy.setAssetAmount(assetAmount);
        Mockito.when(limit_buy.getOrderLimit()).thenReturn(orderLimit);
        Mockito.when(limit_buy.getAsset()).thenReturn(asset);
        Mockito.when(limit_buy.getDate()).thenReturn(ldt);
        Mockito.when(limit_buy.getBuyer()).thenReturn(Mockito.mock(Wallet.class));
        return limit_buy;
    }

    private Limit_Sell createMockedLSell(Asset asset, double orderLimit, double assetAmount, LocalDateTime ldt) {
        Limit_Sell limit_sell = Mockito.mock(Limit_Sell.class, Mockito.RETURNS_DEEP_STUBS);
        Mockito.when(limit_sell.getAssetAmount()).thenCallRealMethod();
        Mockito.doCallRealMethod().when(limit_sell).setAssetAmount(Mockito.anyDouble());
        limit_sell.setAssetAmount(assetAmount);
        Mockito.when(limit_sell.getOrderLimit()).thenReturn(orderLimit);
        Mockito.when(limit_sell.getAsset()).thenReturn(asset);
        Mockito.when(limit_sell.getDate()).thenReturn(ldt);
        Mockito.when(limit_sell.getSeller()).thenReturn(Mockito.mock(Wallet.class));
        return limit_sell;
    }

    @Test
    void checkForMatchingOrders() {
        List<Limit_Buy> expectedLBuys = new ArrayList<>(List.of(limit_buyList[3], limit_buyList[1], limit_buyList[0]));
        Map<Limit_Buy, List<Limit_Sell>> matchedOnLimitBuy = orderMatchingService.checkForMatchingOrders();
        printList(matchedOnLimitBuy);
        for (int i = 0; i < matchedOnLimitBuy.keySet().size(); i++) {
            Limit_Buy actual = new ArrayList<>(matchedOnLimitBuy.keySet()).get(i);
            Limit_Buy expectedLBuy = expectedLBuys.get(i);
            assertTrue(equals(actual, expectedLBuy));
        }
        assertTrue(matchedOnLimitBuy.keySet().containsAll(expectedLBuys));
        System.out.println(limit_buyList[2].getOrderLimit());
        Limit_Buy expectedLBuy = limit_buyList[2];
        assertNull(matchedOnLimitBuy.keySet().stream().filter(k -> equals(k, expectedLBuy)).findFirst().orElse(null));
        List<Limit_Sell> limit_sellsMatched = matchedOnLimitBuy.get(limit_buyList[0]);
        List<Limit_Sell> expectedLSells = new ArrayList<>(List.of(limit_sellList[4], limit_sellList[3], limit_sellList[5], limit_sellList[0], limit_sellList[6]));
        for (int i = 0; i < limit_sellsMatched.size(); i++) {
            Limit_Sell actual = limit_sellsMatched.get(i);
            Limit_Sell expectedLSell = expectedLSells.get(i);
            assertTrue(equals(expectedLSell, actual));
        }
        assertTrue(matchedOnLimitBuy.get(limit_buyList[0]).containsAll(matchedOnLimitBuy.get(limit_buyList[1])));
        assertEquals(1, matchedOnLimitBuy.get(limit_buyList[3]).size());
    }

    @Test
    void createTransActionsFromMatches() {
        Map<Limit_Buy, List<Limit_Sell>> matchedOnLimitBuy = orderMatchingService.checkForMatchingOrders();
        List<Transaction> transActions = orderMatchingService.createTransActionFromMatches(matchedOnLimitBuy);
        assertEquals(7, transActions.size());
        printList(matchedOnLimitBuy);
    }

    @Test
    void createTransActionsFromMatchesEmptyList() {
        Mockito.when(rootRepository.getAllLimitBuy()).thenReturn(new ArrayList<>());
        Mockito.when(rootRepository.getAllLimitSell()).thenReturn(new ArrayList<>());
        Map<Limit_Buy, List<Limit_Sell>> matchedOnLimitBuy = orderMatchingService.checkForMatchingOrders();
        List<Transaction> transActions = orderMatchingService.createTransActionFromMatches(matchedOnLimitBuy);
        assertTrue(transActions.isEmpty());
    }

    @Test
    void procesMatches(){
        Map<Limit_Buy, List<Limit_Sell>> matchedOnLimitBuy = orderMatchingService.checkForMatchingOrders();
        List<Transaction> transActions = orderMatchingService.createTransActionFromMatches(matchedOnLimitBuy);
        printList(orderMatchingService.processOrders(matchedOnLimitBuy));
    }

    private boolean equals(AbstractOrder expectedLBuy, AbstractOrder actual) {
        return expectedLBuy.getDate().equals(actual.getDate()) &&
                expectedLBuy.getOrderLimit() == (actual.getOrderLimit()) &&
                expectedLBuy.getAssetAmount() == actual.getAssetAmount() &&
                expectedLBuy.getAsset().equals(actual.getAsset());
    }


    private void printList(Map<Limit_Buy, List<Limit_Sell>> matchedOnLimitBuy) {
        for (Limit_Buy limit_buy : matchedOnLimitBuy.keySet()) {
            System.out.printf("LimitBuy: assetCode: %s assetAmount: %s orderLimit: %s, date %s\n", limit_buy.getAsset().getCode(), limit_buy.getAssetAmount(), limit_buy.getOrderLimit(), limit_buy.getDate());
            for (Limit_Sell limit_sell : matchedOnLimitBuy.get(limit_buy)) {
                System.out.printf("\tLimitSell: assetCode: %s assetAmount: %s orderLimit: %s, date %s\n", limit_sell.getAsset().getCode(), limit_sell.getAssetAmount(), limit_sell.getOrderLimit(), limit_sell.getDate());
            }
        }
    }


}




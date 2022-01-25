

package com.example.project_bigbangk.service;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.*;
import com.example.project_bigbangk.model.Orders.*;
import com.example.project_bigbangk.repository.RootRepository;

import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;


import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;


import javax.annotation.Resource;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@SpringBootTest
//@DirtiesContext(classMode= DirtiesContext.ClassMode.)

class MatchedOrderMatchingServiceTest {
    @MockBean
    RootRepository rootRepository;

    @MockBean
    BigBangkApplicatie bigBangkApplicatie;

    @Resource
    OrderMatchingService orderMatchingService;

    @MockBean
    TransactionService transactionService;
    LocalDateTime now;

    Limit_Buy[] limit_buyArray = new Limit_Buy[4];
    Limit_Sell[] limit_sellArray = new Limit_Sell[7];
    Stoploss_Sell[] stoploss_sellArray = new Stoploss_Sell[3];
    private static final double FEEPERCENTAGE = 0.25;

    @BeforeEach
    public void setupMocks() {
        now = LocalDateTime.now();
        Asset assetBTC = (createmockedAsset(AssetCode_Name.BTC, 39000));
        Asset assetADA = (createmockedAsset(AssetCode_Name.ADA, 1.07));
        fillOrderArrays(assetBTC, assetADA);
        Mockito.when(rootRepository.getAllLimitBuy()).thenReturn(new ArrayList<>(List.of(limit_buyArray)));
        Mockito.when(rootRepository.getAllLimitSell()).thenReturn(new ArrayList<>(List.of(limit_sellArray)));
        Mockito.when(rootRepository.getAllStopLossSells()).thenReturn(new ArrayList<>(List.of(stoploss_sellArray)));
        Bank bigBangk = Mockito.mock(Bank.class);
        Wallet bankWallet = Mockito.mock(Wallet.class);
        Mockito.when(bankWallet.sufficientAsset(Mockito.any(Asset.class), Mockito.anyDouble())).thenReturn(true);
        Mockito.when(bankWallet.sufficientBalance(Mockito.anyDouble())).thenReturn(true);
        Mockito.when(bigBangk.getWallet()).thenReturn(bankWallet);
        Mockito.when(bigBangk.getFeePercentage()).thenReturn(FEEPERCENTAGE);
        ReflectionTestUtils.setField(orderMatchingService, "bigBangk", bigBangk);
    }

    private void fillOrderArrays(Asset assetBTC, Asset assetADA) {
        limit_buyArray[0] = (createMockedLBuy(assetBTC, 40000, 7, now.minusDays(30)));
        limit_buyArray[1] = (createMockedLBuy(assetBTC, 40000, 1, now.minusDays(60)));
        limit_buyArray[2] = (createMockedLBuy(assetBTC, 11000, 1, now.minusDays(60)));
        limit_buyArray[3] = (createMockedLBuy(assetADA, 1.2, 11, now.minusDays(90)));
        limit_sellArray[0] = (createMockedLSell(assetBTC, 38000, 2.0, now.minusDays(30)));
        limit_sellArray[1] = (createMockedLSell(assetBTC, 44000, 2.0, now.minusDays(60)));
        limit_sellArray[2] = (createMockedLSell(assetADA, 1.1, 10, now.minusDays(90)));
        limit_sellArray[3] = (createMockedLSell(assetBTC, 15000, 1, now.minusDays(90)));
        limit_sellArray[4] = (createMockedLSell(assetBTC, 15000, 2, now.minusDays(120)));
        limit_sellArray[5] = (createMockedLSell(assetBTC, 20000, 1, now.minusDays(30)));
        limit_sellArray[6] = (createMockedLSell(assetBTC, 38000, 1.5, now.minusDays(0)));
        stoploss_sellArray[0] = (createMockedStopLoss(assetBTC, 38000, 1.1, now.minusDays(20)));
        stoploss_sellArray[1] = (createMockedStopLoss(assetBTC, 39000, 1.5, now.minusDays(30)));
        stoploss_sellArray[2] = (createMockedStopLoss(assetBTC, 38000, 1.0, now.minusDays(1)));
    }


    @Test
    void checkForMatchingOrders() {
        List<Limit_Buy> expectedLBuys = fillExpectedBuys();
        List<AbstractOrder> LimitSellListTest = new ArrayList<>(List.of(limit_sellArray));
        List<AbstractOrder> stopLossSells = new ArrayList<>(List.of(stoploss_sellArray));
        stopLossSells = ReflectionTestUtils.invokeMethod(orderMatchingService, "filterTriggeredStopLoss",stopLossSells);
        assertNotNull(stopLossSells);
        LimitSellListTest.addAll(stopLossSells);
        List<MatchedOrder> matchedOrders = ReflectionTestUtils.invokeMethod(orderMatchingService,
                "checkForMatchingOrders",
                new ArrayList<>(List.of(limit_buyArray)), LimitSellListTest);
        assertThat(matchedOrders).isNotNull().size().isEqualTo(13);

        for (MatchedOrder matchedOrder : matchedOrders) {
            System.out.println(matchedOrder);
        }
        for (int i = 0; i < expectedLBuys.size(); i++) {
            Limit_Buy actual = matchedOrders.get(i).getLimit_buy();
            Limit_Buy expectedLBuy = expectedLBuys.get(i);
            assertEquals(actual, expectedLBuy);
        }
        assertTrue(matchedOrders.stream().map(MatchedOrder::getLimit_buy).collect(Collectors.toList()).containsAll(expectedLBuys));
        System.out.println(limit_buyArray[2].getOrderLimit());
        Limit_Buy notExpectedLBuy = limit_buyArray[2];
        assertThat(matchedOrders).noneMatch(mo -> mo.getLimit_buy().equals(notExpectedLBuy));
        //assertNull(matchedOrders.keySet().stream().filter(k -> k.equals(expectedLBuy)).findFirst().orElse(null));

        List<AbstractOrder> expectedLSells = fillExpectedSells();
        for (int i = 0; i < matchedOrders.size(); i++) {
            AbstractOrder actual = matchedOrders.get(i).getLimitSell();
            AbstractOrder expectedLSell = expectedLSells.get(i);
            assertEquals(expectedLSell, actual);
        }
        assertTrue(matchedOrders.stream().map(MatchedOrder::getLimitSell).collect(Collectors.toList()).containsAll(expectedLSells));
        assertEquals(0, matchedOrders.stream().filter(mo -> mo.getLimit_buy().equals(limit_buyArray[2])).count());
    }

    private List<AbstractOrder> fillExpectedSells() {
        List<AbstractOrder> expectedLSells = new ArrayList<>();
        expectedLSells.add(limit_sellArray[2]);
        for (int i = 0; i < 2; i++) {
            expectedLSells.addAll(List.of(limit_sellArray[4], limit_sellArray[3], limit_sellArray[5], limit_sellArray[0], limit_sellArray[6], stoploss_sellArray[1]));

        }

        return expectedLSells;
    }

    private List<Limit_Buy> fillExpectedBuys() {
        List<Limit_Buy> expectedBuys = new ArrayList<>();
        expectedBuys.add(limit_buyArray[3]);

        for (int i = 0; i < 6; i++) {
            expectedBuys.add(limit_buyArray[1]);
        }
        for (int i = 0; i < 6; i++) {
            expectedBuys.add(limit_buyArray[0]);
        }
        return expectedBuys;
    }

    @Test
    void validateOrders() {
        List<Limit_Buy> limitBuyListTest = new ArrayList<>(List.of(limit_buyArray));
        List<AbstractOrder> limitSellListTest = new ArrayList<>(List.of(limit_sellArray));
        List<AbstractOrder> stopLossListTest = new ArrayList<>(List.of(stoploss_sellArray));
        ReflectionTestUtils.invokeMethod(orderMatchingService,
                "validateOrders", limitSellListTest, limitBuyListTest, stopLossListTest);
        assertEquals(0, limitBuyListTest.size());
        assertEquals(7, limitSellListTest.size());
        assertEquals(3, stopLossListTest.size());
    }

    @Test
    void filterTriggeredStopLoss() {
        List<AbstractOrder> stopLossListTest = new ArrayList<>(List.of(stoploss_sellArray));
        stopLossListTest = ReflectionTestUtils.invokeMethod(orderMatchingService, "filterTriggeredStopLoss", stopLossListTest);
        assertThat(stopLossListTest).isNotNull().anyMatch(sl -> sl.equals(stoploss_sellArray[1])).size().isEqualTo(1);
    }

    @Test
    void createTransActionsFromMatches() {
        List<MatchedOrder> matchedOrders = ReflectionTestUtils.invokeMethod(orderMatchingService,
                "checkForMatchingOrders",
                new ArrayList<>(List.of(limit_buyArray)), new ArrayList<>(List.of(limit_sellArray)));
        List<Transaction> transActions = ReflectionTestUtils.invokeMethod(orderMatchingService, "createTransActionsFromMatches", matchedOrders);
        assertThat(transActions).isNotNull().size().isEqualTo(7);
        assertEquals("ADA", transActions.get(0).getAsset().getCode());
        assertEquals(10, transActions.get(0).getAssetAmount());
        assertEquals(10 * 1.1 * FEEPERCENTAGE / 2, transActions.get(0).getFee());
        ThreeTimesThesame(transActions.get(1));
        ThreeTimesThesame(transActions.get(2));
        ThreeTimesThesame(transActions.get(3));
        assertEquals(2, transActions.get(5).getAssetAmount());
    }

    @Test
    void updateOrders() {
        List<MatchedOrder> matchedOrders = ReflectionTestUtils.invokeMethod(orderMatchingService,
                "checkForMatchingOrders",
                new ArrayList<>(List.of(limit_buyArray)), new ArrayList<>(List.of(limit_sellArray)));
        assertThat(matchedOrders).isNotNull();
        System.out.println(matchedOrders);
        ReflectionTestUtils.invokeMethod(orderMatchingService, "createTransActionsFromMatches", matchedOrders);
        System.out.println(matchedOrders);
        ReflectionTestUtils.invokeMethod(orderMatchingService, "updateOrders", matchedOrders);
        System.out.println(matchedOrders);

        assertThat(matchedOrders).isNotNull().size().isEqualTo(0);
        assertEquals(0.5, limit_buyArray[0].getAssetAmount());
        assertEquals(1, limit_buyArray[3].getAssetAmount());
    }

    @Test
    void updateOrders2() {
        List<AbstractOrder> limitSellList = new ArrayList<>(List.of(limit_sellArray));
        List<AbstractOrder> stopLossList = ReflectionTestUtils.invokeMethod(orderMatchingService
                , "filterTriggeredStopLoss", new ArrayList<>(List.of(stoploss_sellArray)));
        assertThat(stopLossList).isNotNull().size().isEqualTo(1);
        limitSellList.addAll(stopLossList);
        List<MatchedOrder> matchedOrders = ReflectionTestUtils.invokeMethod(orderMatchingService,
                "checkForMatchingOrders",
                new ArrayList<>(List.of(limit_buyArray)), limitSellList);
        assertThat(matchedOrders).isNotNull();
        System.out.println(matchedOrders);
        ReflectionTestUtils.invokeMethod(orderMatchingService, "createTransActionsFromMatches", matchedOrders);
        ReflectionTestUtils.invokeMethod(orderMatchingService, "updateOrders", matchedOrders);
        assertThat(matchedOrders).isNotNull().size().isEqualTo(0);
        assertEquals(0, limit_buyArray[0].getAssetAmount());
        assertEquals(1, limit_buyArray[3].getAssetAmount());
        ReflectionTestUtils.invokeMethod(orderMatchingService, "updateLimitSells", stopLossList);
        System.out.println(limitSellList);
        assertThat(stopLossList).isNotNull().contains(stoploss_sellArray[1]).size().isEqualTo(1);
    }

    void ThreeTimesThesame(Transaction transaction) {
        assertEquals(1, transaction.getAssetAmount());
        assertEquals(1 * 15000 * FEEPERCENTAGE / 2, transaction.getFee());
        assertEquals(AssetCode_Name.BTC.getAssetCode(), transaction.getAsset().getCode());
    }

    @Test
    void createTransActionsFromMatchesEmptyList() {
        List<MatchedOrder> matchedOrders = ReflectionTestUtils.invokeMethod(
                orderMatchingService,
                "checkForMatchingOrders",
                new ArrayList<>(), new ArrayList<>());
        List<Transaction> transActions = ReflectionTestUtils.invokeMethod(orderMatchingService, "createTransActionsFromMatches", matchedOrders);
        assertThat(transActions).isNotNull().size().isEqualTo(0);
    }

    @Test
    void matchStoplossWithBank() {
        List<AbstractOrder> stopLossSellsTest = new ArrayList<>(List.of(stoploss_sellArray));

        stopLossSellsTest = ReflectionTestUtils.invokeMethod(orderMatchingService, "filterTriggeredStopLoss", stopLossSellsTest);
        double expected = 1.5;
        assertThat(stopLossSellsTest).isNotNull().size().isNotEqualTo(0);
        assertEquals(expected, stopLossSellsTest.get(0).getAssetAmount());
        ReflectionTestUtils.invokeMethod(orderMatchingService, "matchStopLossWithBank", stopLossSellsTest);
        expected = 0;
        assertEquals(expected, stopLossSellsTest.get(0).getAssetAmount());
    }

    private Asset createmockedAsset(AssetCode_Name assetCodeName, double currentprice) {
        Asset asset = Mockito.when(Mockito.mock(Asset.class).getCurrentPrice()).thenReturn(currentprice).getMock();
        Mockito.when(asset.getCode()).thenReturn(assetCodeName.getAssetCode());
        Mockito.when(asset.getName()).thenReturn(assetCodeName.getAssetName());
        return asset;
    }

    private Limit_Buy createMockedLBuy(Asset asset, double orderLimit, double assetAmount, LocalDateTime ldt) {
        Limit_Buy limit_buy = Mockito.mock(Limit_Buy.class, Mockito.RETURNS_DEEP_STUBS);
        createMockedAbstractOrder(limit_buy, asset, orderLimit, assetAmount, ldt);
        Wallet wallet = Mockito.mock(Wallet.class);
        Mockito.when(wallet.sufficientBalance(Mockito.anyDouble())).thenReturn(false);
        Mockito.when(limit_buy.getBuyer()).thenReturn(wallet);
        return limit_buy;
    }

    private Limit_Sell createMockedLSell(Asset asset, double orderLimit, double assetAmount, LocalDateTime ldt) {
        Limit_Sell limit_sell = Mockito.mock(Limit_Sell.class, Mockito.RETURNS_DEEP_STUBS);
        createMockedAbstractOrder(limit_sell, asset, orderLimit, assetAmount, ldt);
        Wallet wallet = Mockito.mock(Wallet.class);
        Mockito.when(wallet.sufficientAsset(asset, limit_sell.getAssetAmount())).thenReturn(true);
        Mockito.when(limit_sell.getSeller()).thenReturn(wallet);
        return limit_sell;
    }

    private Stoploss_Sell createMockedStopLoss(Asset asset, double orderLimit, double assetAmount, LocalDateTime ldt) {
        Stoploss_Sell stoploss_sell = Mockito.mock(Stoploss_Sell.class, Mockito.RETURNS_DEEP_STUBS);
        createMockedAbstractOrder(stoploss_sell, asset, orderLimit, assetAmount, ldt);
        Wallet wallet = Mockito.mock(Wallet.class);
        Mockito.when(wallet.sufficientAsset(asset, stoploss_sell.getAssetAmount())).thenReturn(true);
        Mockito.when(stoploss_sell.getSeller()).thenReturn(wallet);
        return stoploss_sell;
    }

    private void createMockedAbstractOrder(AbstractOrder abstractOrder, Asset asset, double orderLimit, double assetAmount, LocalDateTime ldt) {
        Mockito.when(abstractOrder.getAssetAmount()).thenCallRealMethod();
        Mockito.doCallRealMethod().when(abstractOrder).setAssetAmount(Mockito.anyDouble());
        abstractOrder.setAssetAmount(assetAmount);
        Mockito.when(abstractOrder.getOrderLimit()).thenReturn(orderLimit);
        Mockito.when(abstractOrder.getAsset()).thenReturn(asset);
        Mockito.when(abstractOrder.getDate()).thenReturn(ldt);
        Mockito.when(abstractOrder.toString()).thenCallRealMethod();
    }


}






package com.example.project_bigbangk.service;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.AssetCode_Name;
import com.example.project_bigbangk.model.Bank;
import com.example.project_bigbangk.model.Orders.*;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;

import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;


import javax.annotation.Resource;

import java.sql.Ref;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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
    Stoploss_Sell[] stoploss_sells = new Stoploss_Sell[3];


    @BeforeEach
    public void setupMocks() {
        now = LocalDateTime.now();
        Asset assetBTC = (createmockedAsset(AssetCode_Name.BTC, 39000));
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
        stoploss_sells[0] = (createMockedStopLoss(assetBTC, 38000, 1.5, now.minusDays(20)));
        stoploss_sells[1] = (createMockedStopLoss(assetBTC, 39000, 1.5, now.minusDays(30)));
        stoploss_sells[2] = (createMockedStopLoss(assetBTC, 38000, 1.5, now.minusDays(1)));
        Mockito.when(rootRepository.getAllLimitBuy()).thenReturn(new ArrayList<>(List.of(limit_buyList)));
        Mockito.when(rootRepository.getAllLimitSell()).thenReturn(new ArrayList<>(List.of(limit_sellList)));
        Mockito.when(rootRepository.getAllStopLossSells()).thenReturn(new ArrayList<>(List.of(stoploss_sells)));
        Bank bigBangk = Mockito.mock(Bank.class);
        Wallet bankWallet = Mockito.mock(Wallet.class);
        Mockito.when(bankWallet.sufficientAsset(Mockito.any(Asset.class), Mockito.anyDouble())).thenReturn(true);
        Mockito.when(bankWallet.sufficientBalance(Mockito.anyDouble())).thenReturn(true);
       Mockito.when(bigBangk.getWallet()).thenReturn(bankWallet);
//        Mockito.doCallRealMethod().when(bankWallet).addToAsset(Mockito.any(Asset.class),Mockito.anyDouble());
//        Mockito.doCallRealMethod().when(bankWallet).addToAsset(Mockito.any(Asset.class),Mockito.anyDouble());
//        Mockito.doCallRealMethod().when(bankWallet).getAssets().get(Mockito.any(Asset.class));
        Mockito.when(bigBangk.getFeePercentage()).thenReturn(0.25);

        ReflectionTestUtils.setField(orderMatchingService, "bigBangk", bigBangk);
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
    }

    @Test
    void checkForMatchingOrders() {
        List<Limit_Buy> expectedLBuys = new ArrayList<>(List.of(limit_buyList[3], limit_buyList[1], limit_buyList[0]));
        List<AbstractOrder> LimitSellListTest = new ArrayList<>(List.of(limit_sellList));
        LimitSellListTest.addAll(ReflectionTestUtils.invokeMethod(orderMatchingService, "filterTriggeredStopLoss", new ArrayList<>(List.of(stoploss_sells))));
        Map<Limit_Buy, List<AbstractOrder>> matchedOnLimitBuy = ReflectionTestUtils.invokeMethod(orderMatchingService,
                "checkForMatchingOrders",
                new ArrayList<>(List.of(limit_buyList)), LimitSellListTest);
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
        List<AbstractOrder> limit_sellsMatched = matchedOnLimitBuy.get(limit_buyList[0]);
        List<AbstractOrder> expectedLSells = new ArrayList<>(List.of(limit_sellList[4], limit_sellList[3], limit_sellList[5], limit_sellList[0], limit_sellList[6], stoploss_sells[1]));
        for (int i = 0; i < limit_sellsMatched.size(); i++) {
            AbstractOrder actual = limit_sellsMatched.get(i);
            AbstractOrder expectedLSell = expectedLSells.get(i);
            assertTrue(equals(expectedLSell, actual));
        }
        assertTrue(matchedOnLimitBuy.get(limit_buyList[0]).containsAll(matchedOnLimitBuy.get(limit_buyList[1])));
        assertEquals(1, matchedOnLimitBuy.get(limit_buyList[3]).size());
    }

    //    List<AbstractOrder> limitSells = rootRepository.getAllLimitSell().stream().map(lso -> (AbstractOrder) lso).collect(Collectors.toList());
//    List<AbstractOrder> stopLossSells = rootRepository.getAllStopLossSells().stream().map(lso -> (AbstractOrder) lso).collect(Collectors.toList());
//    List<Limit_Buy> limit_buys = rootRepository.getAllLimitBuy();
//    limit_buys = sortByDateReversed(limit_buys);
//    validateOrders(limitSells, limit_buys, stopLossSells);
//    stopLossSells = filterTriggeredStopLoss(stopLossSells);
//        limitSells.addAll(stopLossSells);
//    Map<Limit_Buy, List<AbstractOrder>> matchingOrders = checkForMatchingOrders(limit_buys, limitSells);
//    List<Transaction> transactions = createTransActionFromMatches(matchingOrders);
//    updateOrders(matchingOrders);
//        transactions.addAll(matchRemainingStopLossWithBank(stopLossSells));
//    updateLimitSells(stopLossSells);
//    procesTransactions(transactions);
//        logger.info(String.format("Order processed, there where %s matches", transactions.size()));
    @Test
    void validateOrders() {
        List<Limit_Buy> limitBuyListTest = new ArrayList(List.of(limit_buyList));
        List<AbstractOrder> limitSellListTest = new ArrayList(List.of(limit_sellList));
        List<AbstractOrder> stopLossListTest = new ArrayList(List.of(stoploss_sells));
        ReflectionTestUtils.invokeMethod(orderMatchingService,
                "validateOrders", limitSellListTest, limitBuyListTest, stopLossListTest);
        assertEquals(0, limitBuyListTest.size());
        assertEquals(7, limitSellListTest.size());
        assertEquals(3, stopLossListTest.size());
    }

    @Test
    void filterTriggeredStopLoss() {
        List<AbstractOrder> stopLossListTest = new ArrayList(List.of(stoploss_sells));
        stopLossListTest = ReflectionTestUtils.invokeMethod(orderMatchingService, "filterTriggeredStopLoss", stopLossListTest);
        System.out.println(stopLossListTest);
        assertTrue(equals(stoploss_sells[1], stopLossListTest.get(0)));
        assertEquals(1, stopLossListTest.size());
    }

    @Test
    void createTransActionsFromMatches() {
        List<AbstractOrder> LimitSellListTest = new ArrayList<>(List.of(limit_sellList));
        LimitSellListTest.addAll(ReflectionTestUtils.invokeMethod(orderMatchingService, "filterTriggeredStopLoss", new ArrayList<>(List.of(stoploss_sells))));
        Map<Limit_Buy, List<AbstractOrder>> matchedOnLimitBuy = ReflectionTestUtils.invokeMethod(orderMatchingService,
                "checkForMatchingOrders",
                new ArrayList<>(List.of(limit_buyList)), new ArrayList<>(List.of(limit_sellList)));
        List<Transaction> transActions = ReflectionTestUtils.invokeMethod(orderMatchingService, "createTransActionFromMatches", matchedOnLimitBuy);
        assertEquals(7, transActions.size());
        printList(matchedOnLimitBuy);
        assertEquals("ADA", transActions.get(0).getAsset().getCode());
        assertEquals(10, transActions.get(0).getAssetAmount());
        assertEquals(10 * 1.1 * 0.25, transActions.get(0).getFee());
        ThreeTimesThesame(transActions.get(1));
        ThreeTimesThesame(transActions.get(2));
        ThreeTimesThesame(transActions.get(3));
        assertEquals(2, transActions.get(5).getAssetAmount());
    }

    void ThreeTimesThesame(Transaction transaction) {
        assertEquals(transaction.getAssetAmount(), 1);
        assertEquals(transaction.getFee(), 1 * 15000 * 0.25);
        assertEquals(transaction.getAsset().getCode(), "BTC");
    }

    @Test
    void createTransActionsFromMatchesEmptyList() {
        Map<Limit_Buy, List<AbstractOrder>> matchedOnLimitBuy = ReflectionTestUtils.invokeMethod(
                orderMatchingService,
                "checkForMatchingOrders",
                new ArrayList<>(), new ArrayList<>());
        printList(matchedOnLimitBuy);
        List<Transaction> transActions = ReflectionTestUtils.invokeMethod(orderMatchingService, "createTransActionFromMatches", matchedOnLimitBuy);
        assertTrue(transActions.isEmpty());
    }

    @Test
    void matchRemainingStoplossWithBank() {
        List<AbstractOrder> LimitSellListTest = new ArrayList<>(List.of(limit_sellList));
        List<AbstractOrder> stopLossSellsTest = ReflectionTestUtils.invokeMethod(orderMatchingService, "filterTriggeredStopLoss", new ArrayList<>(List.of(stoploss_sells)));
        LimitSellListTest.addAll(stopLossSellsTest);
        Map<Limit_Buy, List<AbstractOrder>> matchedOnLimitBuy = ReflectionTestUtils.invokeMethod(orderMatchingService,
                "checkForMatchingOrders",
                new ArrayList<>(List.of(limit_buyList)), stopLossSellsTest);
        ReflectionTestUtils.invokeMethod(orderMatchingService, "createTransActionFromMatches", matchedOnLimitBuy);
        ReflectionTestUtils.invokeMethod(orderMatchingService, "updateOrdersInDB", matchedOnLimitBuy);
        System.out.println(matchedOnLimitBuy);
        ReflectionTestUtils.invokeMethod(orderMatchingService, "matchRemainingStopLossWithBank", stopLossSellsTest);
        System.out.println(stopLossSellsTest);
    }


    private boolean equals(AbstractOrder expectedLBuy, AbstractOrder actual) {
        return expectedLBuy.getDate().equals(actual.getDate()) &&
                expectedLBuy.getOrderLimit() == (actual.getOrderLimit()) &&
                expectedLBuy.getAssetAmount() == actual.getAssetAmount()
                && expectedLBuy.getAsset().equals(actual.getAsset());
    }


    private void printList(Map<Limit_Buy, List<AbstractOrder>> matchedOnLimitBuy) {
        for (Limit_Buy limit_buy : matchedOnLimitBuy.keySet()) {
            System.out.printf("LimitBuy: assetCode: %s assetAmount: %s orderLimit: %s, date %s\n", limit_buy.getAsset().getCode(), limit_buy.getAssetAmount(), limit_buy.getOrderLimit(), limit_buy.getDate());
            for (AbstractOrder limit_sell : matchedOnLimitBuy.get(limit_buy)) {
                System.out.printf("\tLimitSell: assetCode: %s assetAmount: %s orderLimit: %s, date %s\n", limit_sell.getAsset().getCode(), limit_sell.getAssetAmount(), limit_sell.getOrderLimit(), limit_sell.getDate());
            }
        }
    }


}




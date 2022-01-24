// Created by Deek
// Creation date 1/18/2022

package com.example.project_bigbangk.service;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Bank;
import com.example.project_bigbangk.model.Orders.*;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;
import com.example.project_bigbangk.service.priceHistoryUpdate.IObserver;
import com.example.project_bigbangk.service.priceHistoryUpdate.ISubject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * matches orders by some predefined rules:
 * limit buy ik wil kopen voor een bedrag niet hoger dan limit
 * limit sell ik wil kopen voor een bedrag niet lager dan limit
 *
 * verkoop:
 * als sell prijs onder de buyLimit is dan is er een match
 * Bij meerder matches gaat de laagste Sell Limit eerst
 * Bij gelijke Sell Limits ggat de oudste eerst
 * Bij gelijke Buy Limits gaat de oudste eerst
 *
 * StopLossSell:
 * als currentPrice onder limit dan verkopen
 * als er een match is met een LimitBuy dan die voorrang
 * anders verkopen aan de bank
 */

@Service
public class OrderMatchingService implements IObserver {

    private RootRepository rootRepository;
    private BigBangkApplicatie bigBangkApplicatie;
    private final Logger logger = LoggerFactory.getLogger(OrderMatchingService.class);
    private TransactionService transactionService;
    private ISubject priceHistoryUpdateService;
    private Bank bigBangk;

    public OrderMatchingService(RootRepository rootRepository, BigBangkApplicatie bigBangkApplicatie, TransactionService transactionService, ISubject priceHistroyUpdateService) {
        super();
        logger.info("New OrderMatchingService");
        this.rootRepository = rootRepository;
        this.bigBangkApplicatie = bigBangkApplicatie;
        this.transactionService = transactionService;
        this.priceHistoryUpdateService = priceHistroyUpdateService;
        priceHistroyUpdateService.addListener(this);
        bigBangk = bigBangkApplicatie.getBank();
    }

    @Override
    public void update() {
        List<AbstractOrder> limitSells = rootRepository.getAllLimitSell().stream().map(lso -> (AbstractOrder) lso).collect(Collectors.toList());
        List<AbstractOrder> stopLossSells = rootRepository.getAllStopLossSells().stream().map(lso -> (AbstractOrder) lso).collect(Collectors.toList());
        List<Limit_Buy> limit_buys = rootRepository.getAllLimitBuy();
        limit_buys = sortByDateReversed(limit_buys);
        validateOrders(limitSells, limit_buys, stopLossSells);
        stopLossSells = filterTriggeredStopLoss(stopLossSells);
        limitSells.addAll(stopLossSells);
        Map<Limit_Buy, List<AbstractOrder>> matchingOrders = checkForMatchingOrders(limit_buys, limitSells);
        List<Transaction> transactions = createTransActionFromMatches(matchingOrders);
        updateOrdersInDB(matchingOrders);
        transactions.addAll(matchRemainingStopLossWithBank(stopLossSells));
        updateLimitSellsInDB(stopLossSells);
        procesTransactions(transactions);
        logger.info(String.format("Order processed, there where %s matches", transactions.size()));
    }

    private List<Transaction> matchRemainingStopLossWithBank(List<AbstractOrder> stopLossSells) {
        List<Transaction> transactions = new ArrayList<>();
            for (AbstractOrder abstractOrder : stopLossSells) {
                if (abstractOrder instanceof Stoploss_Sell) {
                    Stoploss_Sell stoploss_sell = (Stoploss_Sell) abstractOrder;
                    //eigenlijk nog een validate van de order???
                    double priceExcludingFee = stoploss_sell.getAssetAmount() * stoploss_sell.getAsset().getCurrentPrice();
                    double fee = priceExcludingFee * bigBangk.getFeePercentage();
                    Transaction transaction = new Transaction(stoploss_sell.getAsset(),
                            priceExcludingFee,
                            stoploss_sell.getAssetAmount(),
                            LocalDateTime.now(), fee, bigBangk.getWallet(), stoploss_sell.getSeller());
                    if (bigBangk.getWallet().sufficientBalance(priceExcludingFee - fee)) {
                        transactions.add(transaction);
                        bigBangk.getWallet().removeFromBalance(priceExcludingFee - fee);
                        bigBangk.getWallet().addToAsset(stoploss_sell.getAsset(), stoploss_sell.getAssetAmount());
                        System.out.println(bigBangk.getWallet().getAssets().get(stoploss_sell.getAsset()));
                        stoploss_sell.getSeller().addToBalance(priceExcludingFee - fee);
                        stoploss_sell.setAssetAmount(0);
                    }
                }
            }
        return transactions;
    }

    private List<Limit_Buy> sortByDateReversed(List<Limit_Buy> limit_buys) {
        return limit_buys.stream()
                .sorted(Comparator.comparing(AbstractOrder::getDate).reversed())
                .collect(Collectors.toList());
    }

    private void validateOrders(List<AbstractOrder> limitSells, List<Limit_Buy> limit_buys, List<AbstractOrder> stopLossSells) {
        validateBuyOrders(limit_buys);
        validateLSellOrders(limitSells);
        validateLSellOrders(stopLossSells);
    }

    private void procesTransactions(List<Transaction> transactions) {
        for (Transaction transaction : transactions) {
            transactionService.processTransaction(transaction);
        }
    }

    private void validateBuyOrders(List<Limit_Buy> allLimit_buyOrders) {
        List<Limit_Buy> inValidOrders = new ArrayList<>();
        for (Limit_Buy limit_buy : allLimit_buyOrders) {
            double orderPrice = limit_buy.getAssetAmount() * limit_buy.getOrderLimit();
            double fee = bigBangk.getFeePercentage();
            if (!limit_buy.getBuyer().sufficientBalance(orderPrice + fee / 2)) {
                inValidOrders.add(limit_buy);
                rootRepository.deleteOrderByID(limit_buy.getOrderId());
            }
        }
        allLimit_buyOrders.removeAll(inValidOrders);
    }

    private void validateLSellOrders(List<AbstractOrder> allLimit_Sells) {
        List<AbstractOrder> inValidOrders = new ArrayList<>();
        for (AbstractOrder abstractOrder : allLimit_Sells) {
            if (!getWallet(abstractOrder).sufficientAsset(abstractOrder.getAsset(), abstractOrder.getAssetAmount())) {
                rootRepository.deleteOrderByID(abstractOrder.getOrderId());
                inValidOrders.add(abstractOrder);
            }
        }
        allLimit_Sells.removeAll(inValidOrders);
    }


    //limit buy ik wil kopen voor een bedrag niet hoger dan limit
    //limit sell ik wil kopen voor een bedrag niet lager dan limit
    private Map<Limit_Buy, List<AbstractOrder>> checkForMatchingOrders
    (List<Limit_Buy> allLimit_BuyOrders, List<AbstractOrder> allLimit_SellOrders) {
        Map<Limit_Buy, List<AbstractOrder>> matchesByLimitBuy = new TreeMap<>(Comparator.comparing(Limit_Buy::getDate));
        for (Limit_Buy limit_buy : allLimit_BuyOrders) {
            List<AbstractOrder> matches = allLimit_SellOrders.stream()
                    .filter(lso -> limit_buy.getOrderLimit() >= lso.getOrderLimit()
                            && limit_buy.getAsset().equals(lso.getAsset()) && !limit_buy.getBuyer().equals(getWallet(lso)))
                    .sorted(new lowestPriceThenOldest())
                    .collect(Collectors.toList());
            if (matches.size() > 0) {
                matchesByLimitBuy.put(limit_buy, matches);
            }
        }
        return matchesByLimitBuy;
    }

    private Wallet getWallet(AbstractOrder order) {
        if (order instanceof Limit_Sell) {
            return ((Limit_Sell) order).getSeller();
        } else if (order instanceof Stoploss_Sell) {
            return ((Stoploss_Sell) order).getSeller();
        } else {
            return ((Limit_Buy) order).getBuyer();
        }
    }

    private List<AbstractOrder> filterTriggeredStopLoss(List<AbstractOrder> stopLossSells) {
        return stopLossSells.stream()
                .filter(sl -> sl.getAsset().getCurrentPrice() <= sl.getOrderLimit())
                .collect(Collectors.toList());
    }

    private List<Transaction> createTransActionFromMatches
            (Map<Limit_Buy, List<AbstractOrder>> matchedOnLimitBuy) {
        List<Transaction> transactions = new ArrayList<>();
        for (Limit_Buy limit_buy : matchedOnLimitBuy.keySet()) {
            double amountOfAssetsToBuy = limit_buy.getAssetAmount();
            List<AbstractOrder> matchedLimitSells = matchedOnLimitBuy.get(limit_buy);
            int indexLimitSell = 0;
            while (amountOfAssetsToBuy > 0 && indexLimitSell < matchedLimitSells.size()) {
                AbstractOrder limit_sellMatch = matchedLimitSells.get(indexLimitSell);
                double amountOfAssetsLimitSell = limit_sellMatch.getAssetAmount();
                double transactionAssetAmount = amountOfAssetsToBuy - amountOfAssetsLimitSell < 0 ? amountOfAssetsToBuy : amountOfAssetsLimitSell;
                double priceExcludingFee = transactionAssetAmount * limit_sellMatch.getOrderLimit();
                double transActionfee = transactionAssetAmount * limit_sellMatch.getOrderLimit() * bigBangk.getFeePercentage();
                Transaction transactionPending = new Transaction(limit_buy.getAsset(),
                        priceExcludingFee,
                        transactionAssetAmount,
                        LocalDateTime.now(), transActionfee, limit_buy.getBuyer(), getWallet(limit_sellMatch));
                transactions.add(transactionPending);
                amountOfAssetsToBuy -= transactionAssetAmount;
                limit_sellMatch.setAssetAmount(amountOfAssetsLimitSell - transactionAssetAmount);
                limit_buy.setAssetAmount(amountOfAssetsToBuy);
                indexLimitSell++;
            }
        }
        return transactions;
    }

    private void createTransactionFromMatch(Limit_Buy limit_buy, List<Limit_Sell> matchedLimitSells) {

    }

    private Map<Limit_Buy, List<AbstractOrder>> updateOrdersInDB
            (Map<Limit_Buy, List<AbstractOrder>> matchedOnLimitBuy) {
        for (Limit_Buy limit_buy : matchedOnLimitBuy.keySet()) {
            if (limit_buy.getAssetAmount() == 0) {
                rootRepository.deleteOrderByID(limit_buy.getOrderId());
            } else {
                rootRepository.updateLimitBuy(limit_buy);
            }
           updateLimitSellsInDB(matchedOnLimitBuy.get(limit_buy));
        }
        return matchedOnLimitBuy;
    }

    private void updateLimitSellsInDB(List<AbstractOrder> limitSells) {
        for (AbstractOrder limit_sell : limitSells) {
            if (limit_sell.getAssetAmount() == 0) {
                rootRepository.deleteOrderByID(limit_sell.getOrderId());
            } else {
                if (limit_sell instanceof Limit_Sell) {
                    rootRepository.updateLimitSell((Limit_Sell) limit_sell);
                } else {
                    rootRepository.updateStopLoss((Stoploss_Sell) limit_sell);
                }
            }
        }
    }


    private static class lowestPriceThenOldest implements Comparator<AbstractOrder> {
        @Override
        public int compare(AbstractOrder o1, AbstractOrder o2) {
            return (o1.getOrderLimit() - o2.getOrderLimit()) == 0 ? o1.getDate().compareTo(o2.getDate()) :
                    (o1.getOrderLimit() - o2.getOrderLimit()) > 0 ? 1 : -1;
        }
    }
}

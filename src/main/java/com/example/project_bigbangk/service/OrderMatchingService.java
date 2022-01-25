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
 * <p>
 * verkoop:
 * als sell prijs onder de buyLimit is dan is er een match
 * Bij meerder matches gaat de laagste Sell Limit eerst
 * Bij gelijke Sell Limits ggat de oudste eerst
 * Bij gelijke Buy Limits gaat de oudste eerst
 * <p>
 * StopLossSell:
 * als currentPrice onder limit dan verkopen
 * als er een match is met een LimitBuy dan die voorrang
 * anders verkopen aan de bank
 *
 * Trnasaction:
 * Als een tranasaction met de klant is wordt de helft van de fee opgeslagen.
 * Als een transaction met de bank is word de hele fee opgeslagen
 * @author Pieter jan bleichrodt
 * */

@Service
public class OrderMatchingService implements IObserver {

    private RootRepository rootRepository;
    private BigBangkApplicatie bigBangkApplicatie;
    private final Logger logger = LoggerFactory.getLogger(OrderMatchingService.class);
    private TransactionService transactionService;
    private ISubject priceHistoryUpdateService;
    private Bank bigBangk;

    public OrderMatchingService(RootRepository rootRepository, BigBangkApplicatie bigBangkApplicatie, TransactionService transactionService, ISubject priceHistoryUpdateService) {
        super();
        logger.info("New OrderMatchingService");
        this.rootRepository = rootRepository;
        this.bigBangkApplicatie = bigBangkApplicatie;
        this.transactionService = transactionService;
        this.priceHistoryUpdateService = priceHistoryUpdateService;
        priceHistoryUpdateService.addListener(this);
        bigBangk = bigBangkApplicatie.getBank();
    }

    /**
     * this method is called whenever the subject, in this case the PriceHistoryUpdateService, does notify its listeners.
     * Then method searches for matches in the saved orders. For each match a transaction is made and the order is updates accordingly.
     * If the assetAmount of an order is not 0 then the subsequent match is processed into a transaction.
     * For each triggered StopLossSell we search for a match with a LimitBuy. If there's none, a match with the bank will be made.
     */
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
        List<Transaction> transactions = createTransActionsFromAllMatches(matchingOrders);
        updateOrders(matchingOrders);
        transactions.addAll(matchStopLossWithBank(stopLossSells));
        updateLimitSellsAndStopLoss(stopLossSells);
        procesTransactions(transactions);
        logger.info(String.format("Order processed, there where %s matches", transactions.size()));
    }

    private List<Transaction> matchStopLossWithBank(List<AbstractOrder> stopLossSells) {
        List<Transaction> transactions = new ArrayList<>();
        for (AbstractOrder abstractOrder : stopLossSells) {
            if (abstractOrder instanceof Stoploss_Sell) {
                Transaction transaction = createTransActionWithbank(abstractOrder);
                if (transaction != null) {
                    transactions.add(transaction);
                }
            }
        }
        return transactions;
    }

    private Transaction createTransActionWithbank(AbstractOrder abstractOrder) {
        Stoploss_Sell stoploss_sell = (Stoploss_Sell) abstractOrder;
        double priceExcludingFee = stoploss_sell.getAssetAmount() * stoploss_sell.getAsset().getCurrentPrice();
        double fee = priceExcludingFee * bigBangk.getFeePercentage();
        if (bigBangk.getWallet().sufficientBalance(priceExcludingFee - fee)) {
            bigBangk.getWallet().removeFromBalance(priceExcludingFee - fee);
            bigBangk.getWallet().addToAsset(stoploss_sell.getAsset(), stoploss_sell.getAssetAmount());
            System.out.println(bigBangk.getWallet().getAssets().get(stoploss_sell.getAsset()));
            stoploss_sell.getSeller().addToBalance(priceExcludingFee - fee);
            stoploss_sell.setAssetAmount(0);
            return new Transaction(stoploss_sell.getAsset(),
                    priceExcludingFee,
                    stoploss_sell.getAssetAmount(),
                    LocalDateTime.now(), fee, bigBangk.getWallet(), stoploss_sell.getSeller());
        } else {
            return null;
        }
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

    private List<Transaction> createTransActionsFromAllMatches(Map<Limit_Buy, List<AbstractOrder>> matchedOnLimitBuy) {
        List<Transaction> transactions = new ArrayList<>();
        for (Limit_Buy limit_buy : matchedOnLimitBuy.keySet()) {
            List<AbstractOrder> matchedLimitSells = matchedOnLimitBuy.get(limit_buy);
            transactions.addAll(createTransActionsFromKeyValue(limit_buy, matchedLimitSells));
        }
        return transactions;
    }

    private List<Transaction> createTransActionsFromKeyValue(Limit_Buy limit_buy, List<AbstractOrder> matchedLimitSells) {
        int indexLimitSell = 0;
        List<Transaction> transactions = new ArrayList<>();
        while (limit_buy.getAssetAmount() > 0 && indexLimitSell < matchedLimitSells.size()) {
            AbstractOrder limit_sellMatch = matchedLimitSells.get(indexLimitSell);
            transactions.add(createTransactionFromMatch(limit_buy, limit_sellMatch));
            indexLimitSell++;
        }
        return transactions;
    }

    private Transaction createTransactionFromMatch(Limit_Buy limit_buy, AbstractOrder limit_sellMatch) {
        double thisTransactionAssetAmount = limit_buy.getAssetAmount() - limit_sellMatch.getAssetAmount() < 0 ? limit_buy.getAssetAmount() : limit_sellMatch.getAssetAmount();
        double priceExcludingFee = thisTransactionAssetAmount * limit_sellMatch.getOrderLimit();
        double transActionfee = thisTransactionAssetAmount * limit_sellMatch.getOrderLimit() * bigBangk.getFeePercentage() / 2;
        limit_buy.setAssetAmount(limit_buy.getAssetAmount() - thisTransactionAssetAmount);
        limit_sellMatch.setAssetAmount(limit_sellMatch.getAssetAmount() - thisTransactionAssetAmount);
        return new Transaction(limit_buy.getAsset(),
                priceExcludingFee,
                thisTransactionAssetAmount,
                LocalDateTime.now(), transActionfee, limit_buy.getBuyer(), getWallet(limit_sellMatch));
    }

    private Map<Limit_Buy, List<AbstractOrder>> updateOrders(Map<Limit_Buy, List<AbstractOrder>> matchedOnLimitBuy) {
        List<Limit_Buy> limitBuyToRemove = new ArrayList<>();

        for (Limit_Buy limit_buy : matchedOnLimitBuy.keySet()) {
            if (limit_buy.getAssetAmount() == 0) {
                rootRepository.deleteOrderByID(limit_buy.getOrderId());
                limitBuyToRemove.add(limit_buy);
            } else {
                rootRepository.updateLimitBuy(limit_buy);

            }
            updateLimitSellsAndStopLoss(matchedOnLimitBuy.get(limit_buy));
        }
        limitBuyToRemove.forEach(matchedOnLimitBuy.keySet()::remove);
        return matchedOnLimitBuy;
    }

    private void updateLimitSellsAndStopLoss(List<AbstractOrder> limitSells) {
        List<AbstractOrder> limitSellsToRemove = new ArrayList<>();
        for (AbstractOrder limit_sell : limitSells) {
            if (limit_sell.getAssetAmount() == 0) {
                rootRepository.deleteOrderByID(limit_sell.getOrderId());
                limitSellsToRemove.add(limit_sell);
            } else {
                if (limit_sell instanceof Limit_Sell) {
                    rootRepository.updateLimitSell((Limit_Sell) limit_sell);
                } else {
                    rootRepository.updateStopLoss((Stoploss_Sell) limit_sell);
                }
            }
        }
        limitSells.removeAll(limitSellsToRemove);
    }


    private static class lowestPriceThenOldest implements Comparator<AbstractOrder> {
        @Override
        public int compare(AbstractOrder o1, AbstractOrder o2) {
            return (o1.getOrderLimit() - o2.getOrderLimit()) == 0 ? o1.getDate().compareTo(o2.getDate()) :
                    (o1.getOrderLimit() - o2.getOrderLimit()) > 0 ? 1 : -1;
        }
    }
}

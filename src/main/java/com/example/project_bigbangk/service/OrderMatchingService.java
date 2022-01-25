// Created by Deek
// Creation date 1/18/2022

package com.example.project_bigbangk.service;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Bank;
import com.example.project_bigbangk.model.MatchedOrder;
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
 * <p>
 * Trnasaction:
 * Als een tranasaction met de klant is wordt de helft van de fee opgeslagen.
 * Als een transaction met de bank is word de hele fee opgeslagen
 *
 * @author Pieter jan bleichrodt
 */

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
        System.out.println(limit_buys);
        validateOrders(limitSells, limit_buys, stopLossSells);
        stopLossSells = filterTriggeredStopLoss(stopLossSells);
        limitSells.addAll(stopLossSells);

        List<MatchedOrder> matchingOrders = checkForMatchingOrders(limit_buys, limitSells);
        List<Transaction> transactions = createTransActionsFromMatches(matchingOrders);
        updateOrders(matchingOrders);

        updateLimitSells(stopLossSells);
        transactions.addAll(matchStopLossWithBank(stopLossSells));
        updateLimitSells(stopLossSells);
        procesTransactions(transactions);
        logger.info(String.format("Order processed, there where %s matches", transactions.size()));
    }


    private void updateLimitSells(List<AbstractOrder> stopLossSells) {
        List<AbstractOrder> stopLossToDelete = new ArrayList<>();
        for (AbstractOrder abstractOrder : stopLossSells) {
            if(updateLimitSell(abstractOrder)){
                stopLossToDelete.add(abstractOrder);
            }
        }
        stopLossSells.removeAll(stopLossToDelete);
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

    private List<MatchedOrder> checkForMatchingOrders(List<Limit_Buy> allLimit_BuyOrders, List<AbstractOrder> allLimit_SellOrders) {
        List<MatchedOrder> matchedOrders = new ArrayList<>();
        for (Limit_Buy limit_buy : allLimit_BuyOrders) {
            List<AbstractOrder> matchingSells = allLimit_SellOrders.stream()
                    .filter(lso -> limit_buy.getOrderLimit() >= lso.getOrderLimit()
                            && limit_buy.getAsset().equals(lso.getAsset()) && !limit_buy.getBuyer().equals(getWallet(lso)))
                    .sorted(new lowestPriceThenOldest())
                    .collect(Collectors.toList());
            if (matchingSells.size() > 0) {
                for (AbstractOrder abstractOrder : matchingSells)
                    matchedOrders.add(new MatchedOrder(limit_buy, abstractOrder));
            }
        }
        matchedOrders.sort(new lowestPriceThenOldest2());
        return matchedOrders;
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

    private List<Transaction> createTransActionsFromMatches(List<MatchedOrder> matchedOrders) {
        List<Transaction> transactions = new ArrayList<>();
        for (MatchedOrder matchedOrder : matchedOrders) {
            if (matchedOrder.getLimit_buy().getAssetAmount() > 0 && matchedOrder.getLimitSell().getAssetAmount() > 0) {
                transactions.add(createTransactionFromMatch(matchedOrder));
            }
        }
        return transactions;
    }

    private Transaction createTransactionFromMatch(MatchedOrder matchedOrder) {
        Limit_Buy limit_buy = matchedOrder.getLimit_buy();
        AbstractOrder limitSell = matchedOrder.getLimitSell();
        double thisTransactionAssetAmount = calculateTransActionAssetAmount(matchedOrder);
        double priceExcludingFee = thisTransactionAssetAmount * limitSell.getOrderLimit();
        double transActionfee = thisTransactionAssetAmount * limitSell.getOrderLimit() * bigBangk.getFeePercentage() / 2;
        limit_buy.setAssetAmount(limit_buy.getAssetAmount() - thisTransactionAssetAmount);
        limitSell.setAssetAmount(limitSell.getAssetAmount() - thisTransactionAssetAmount);
        return new Transaction(limit_buy.getAsset(),
                priceExcludingFee,
                thisTransactionAssetAmount,
                LocalDateTime.now(), transActionfee, limit_buy.getBuyer(), getWallet(limitSell));
    }

    private double calculateTransActionAssetAmount(MatchedOrder matchedOrder) {
        if (matchedOrder.getLimit_buy().getAssetAmount() - matchedOrder.getLimitSell().getAssetAmount() < 0) {
            return matchedOrder.getLimit_buy().getAssetAmount();
        } else {
            return matchedOrder.getLimitSell().getAssetAmount();
        }
    }

    private List<MatchedOrder> updateOrders(List<MatchedOrder> matchedOrders) {
        Set<MatchedOrder> matchedOrdersToRemove = new HashSet<>();
        for (MatchedOrder matchedOrder : matchedOrders) {
            if (updateLimitBuy(matchedOrder.getLimit_buy())) {
                matchedOrdersToRemove.add(matchedOrder);
            }
            if (updateLimitSell(matchedOrder.getLimitSell())) {
                matchedOrdersToRemove.add(matchedOrder);
            }
        }
        matchedOrders.removeAll(matchedOrdersToRemove);
        return matchedOrders;
    }

    private boolean updateLimitBuy(Limit_Buy limit_buy) {
        if (limit_buy.getAssetAmount() == 0) {
            rootRepository.deleteOrderByID(limit_buy.getOrderId());
            return true;
        } else {
            rootRepository.updateLimitBuy(limit_buy);
            return false;
        }
    }

    private boolean updateLimitSell(AbstractOrder limitSell) {
        if (limitSell.getAssetAmount() == 0) {
            rootRepository.deleteOrderByID(limitSell.getOrderId());
            return true;
        } else {
            if (limitSell instanceof Limit_Sell) {
                rootRepository.updateLimitSell((Limit_Sell) limitSell);
            } else {
                rootRepository.updateStopLoss((Stoploss_Sell) limitSell);
            }
        }
        return false;
    }


    private static class lowestPriceThenOldest implements Comparator<AbstractOrder> {
        @Override
        public int compare(AbstractOrder o1, AbstractOrder o2) {
            return (o1.getOrderLimit() - o2.getOrderLimit()) == 0 ? o1.getDate().compareTo(o2.getDate()) :
                    (o1.getOrderLimit() - o2.getOrderLimit()) > 0 ? 1 : -1;
        }
    }

    private static class lowestPriceThenOldest2 implements Comparator<MatchedOrder> {
        @Override
        public int compare(MatchedOrder o1, MatchedOrder o2) {
            double compare = o1.getLimit_buy().getDate().compareTo(o2.getLimit_buy().getDate());
            if (compare == 0) {
                compare = o1.getLimitSell().getOrderLimit() - o2.getLimitSell().getOrderLimit();
                if (compare == 0) {
                    return o1.getLimitSell().getDate().compareTo(o2.getLimitSell().getDate());
                } else {
                    return compare > 0 ? 1 : -1;
                }
            } else {
                return compare > 0 ? 1 : -1;
            }
        }
    }
}

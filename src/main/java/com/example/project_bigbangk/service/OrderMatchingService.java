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
import org.apache.tomcat.util.threads.LimitLatch;
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
 * verkoop
 *
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

    RootRepository rootRepository;
    BigBangkApplicatie bigBangkApplicatie;
    private final Logger logger = LoggerFactory.getLogger(OrderMatchingService.class);
    TransactionService transactionService;
    ISubject priceHistroyUpdateService;

    public OrderMatchingService(RootRepository rootRepository, BigBangkApplicatie bigBangkApplicatie, TransactionService transactionService, ISubject priceHistroyUpdateService) {
        super();
        logger.info("New OrderMatchingService");
        this.rootRepository = rootRepository;
        this.bigBangkApplicatie = bigBangkApplicatie;
        this.transactionService = transactionService;
        this.priceHistroyUpdateService = priceHistroyUpdateService;
        priceHistroyUpdateService.addListener(this);
    }

    @Override
    public void update() {
        Map<Limit_Buy, List<Limit_Sell>> matchingOrders = checkForMatchingOrders();
        List<Transaction> transactions = createTransActionFromMatches(matchingOrders);
        for (Transaction transaction : transactions) {
            transactionService.processTransaction(transaction);
        }
        processOrders(matchingOrders);
        logger.info(String.format("Order processed, there where %s matches", transactions.size()));
    }

    //limit buy ik wil kopen voor een bedrag niet hoger dan limit
    //limit sell ik wil kopen voor een bedrag niet lager dan limit
    public Map<Limit_Buy, List<Limit_Sell>> checkForMatchingOrders() {
        List<Limit_Sell> allLimit_SellOrders = rootRepository.getAllLimitSell();
        List<Limit_Buy> allLimit_BuyOrders = rootRepository.getAllLimitBuy().stream()
                .sorted(Comparator.comparing(AbstractOrder::getDate).reversed())
                .collect(Collectors.toList());
        Map<Limit_Buy, List<Limit_Sell>> matchesByLimitBuy = new TreeMap<>(Comparator.comparing(Limit_Buy::getDate));
        for (Limit_Buy limit_buy : allLimit_BuyOrders) {
            List<Limit_Sell> matches = allLimit_SellOrders.stream()
                    .filter(lso -> limit_buy.getOrderLimit() >= lso.getOrderLimit()
                            && limit_buy.getAsset().equals(lso.getAsset())&&!limit_buy.getBuyer().equals(lso.getSeller()))
                    .sorted(new lowestPriceThenOldest())
                    .collect(Collectors.toList());
            if (matches.size() > 0) {
                matchesByLimitBuy.put(limit_buy, matches);
            }
        }
        return matchesByLimitBuy;
    }

    public List<Stoploss_Sell> checkForStopLoss() {
        List<Stoploss_Sell> stoploss_sells = rootRepository.getAllStopLossSells();
        List<Limit_Buy> limit_buys = rootRepository.getAllLimitBuy();
        for (Stoploss_Sell stoploss_sell : stoploss_sells) {
            if (stoploss_sell.getAsset().getCurrentPrice() >= stoploss_sell.getOrderLimit()) {
                Limit_Buy matchingLBuy = limit_buys.stream()
                        .filter(lbo -> lbo.getOrderLimit() > stoploss_sell.getOrderLimit()).min(Comparator.comparing(AbstractOrder::getDate)).orElse(null);
                if (matchingLBuy != null) {
                    //match met bestaan Limit buy
                } else {
                    //verkoop aan bank
                }
            } else {
                stoploss_sells.remove(stoploss_sell);
            }
        }
        //todo matching list
        return stoploss_sells;
    }

    public List<Transaction> createTransActionFromMatches(Map<Limit_Buy, List<Limit_Sell>> matchedOnLimitBuy) {
        Bank bigBangk = bigBangkApplicatie.getBank();
        List<Transaction> transactions = new ArrayList<>();
        for (Limit_Buy limit_buy : matchedOnLimitBuy.keySet()) {
            // System.out.println(limit_buy);
            double amountOfAssetsToBuy = limit_buy.getAssetAmount();
            int indexLimitSell = 0;
            List<Limit_Sell> matchedLimitSells = matchedOnLimitBuy.get(limit_buy);
            while (amountOfAssetsToBuy > 0 && indexLimitSell < matchedLimitSells.size()) {
                Limit_Sell limit_sellMatch = matchedLimitSells.get(indexLimitSell);
                double amountOfAssetsLimitSell = limit_sellMatch.getAssetAmount();
                double transactionAssetAmount = amountOfAssetsToBuy - amountOfAssetsLimitSell < 0 ? amountOfAssetsToBuy : amountOfAssetsLimitSell;
                double priceExcludingFee = transactionAssetAmount * limit_sellMatch.getOrderLimit();
                double transActionfee = transactionAssetAmount * limit_sellMatch.getOrderLimit() * bigBangk.getFeePercentage();
                Transaction transactionPending = new Transaction(limit_buy.getAsset(),
                        priceExcludingFee,
                        transactionAssetAmount,
                        LocalDateTime.now(), transActionfee, limit_buy.getBuyer(), limit_sellMatch.getSeller());
                if (transactionService.validateTransAction(transactionPending)) {
                    transactions.add(transactionPending);
                    amountOfAssetsToBuy -= transactionAssetAmount;
                    limit_sellMatch.setAssetAmount(amountOfAssetsLimitSell - transactionAssetAmount);
                    limit_buy.setAssetAmount(amountOfAssetsToBuy);
                    //ToDo verwijder limit sell als amounOfAsset 0 is
                }
                indexLimitSell++;
            }
        }
        matchedOnLimitBuy.entrySet().removeIf(lbs -> lbs.getKey().getAssetAmount() == 0);

        return transactions;
    }

    public Map<Limit_Buy, List<Limit_Sell>> processOrders(Map<Limit_Buy, List<Limit_Sell>> matchedOnLimitBuy) {
        for (Limit_Buy limit_buy : matchedOnLimitBuy.keySet()) {
            if (limit_buy.getAssetAmount() == 0) {
                rootRepository.deleteOrderByID(limit_buy.getOrderId());
            } else {
                rootRepository.updateLimitBuy(limit_buy);
            }
            for (Limit_Sell limit_sell : matchedOnLimitBuy.get(limit_buy)) {
                if (limit_buy.getAssetAmount() == 0) {
                    rootRepository.deleteOrderByID(limit_buy.getOrderId());
                } else {
                    rootRepository.updateLimitSell(limit_sell);
                }
            }
        }
        return matchedOnLimitBuy;
    }


    private static class lowestPriceThenOldest implements Comparator<Limit_Sell> {
        @Override
        public int compare(Limit_Sell o1, Limit_Sell o2) {
            return (o1.getOrderLimit() - o2.getOrderLimit()) == 0 ? o1.getDate().compareTo(o2.getDate()) :
                    (o1.getOrderLimit() - o2.getOrderLimit()) > 0 ? 1 : -1;
        }
    }
}

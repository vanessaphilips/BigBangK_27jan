// Created by Deek
// Creation date 1/18/2022

package com.example.project_bigbangk.service;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Orders.*;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;
import org.apache.tomcat.util.threads.LimitLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderMatchingService {

    RootRepository rootRepository;

    private final Logger logger = LoggerFactory.getLogger(OrderMatchingService.class);

    public OrderMatchingService(RootRepository rootRepository) {
        super();
        logger.info("New OrderMatchingService");
        this.rootRepository = rootRepository;
    }

    //limit buy ik wil kopen voor een bedrag niet hoger dan limit
    //limit sell ik wil kopen voor een bedrag niet lager dan limit
    public Map<Limit_Buy, List<Limit_Sell>> checkForMatchingOrders() {
        List<Limit_Sell> allLimit_SellOrders = rootRepository.getAllLimitSell();
        List<Limit_Buy> allLimit_BuyOrders = rootRepository.getAllLimitBuy().stream()
                .sorted(Comparator.comparing(AbstractOrder::getDate).reversed())
                .collect(Collectors.toList());
        Map<Limit_Buy, List<Limit_Sell>> matchesByLimitBuy = new HashMap<>();
        for (Limit_Buy limit_buy : allLimit_BuyOrders) {
            List<Limit_Sell> matches = allLimit_SellOrders.stream()
                    .filter(lso -> limit_buy.getOrderLimit() >= lso.getOrderLimit()
                            && limit_buy.getAsset().equals(lso.getAsset()))
                    .sorted(new lowestPriceThenOldest())
                    .collect(Collectors.toList());
            if (matches.size() > 0) {
                matchesByLimitBuy.put(limit_buy, matches);
            }
        }
        return matchesByLimitBuy;
    }

    public List<Transaction> createTransActionFromMatches(Map<Limit_Buy, List<Limit_Sell>> matchedOnLimitBuy) {
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
                double transActionfee = transactionAssetAmount * limit_sellMatch.getOrderLimit() * BigBangkApplicatie.getBank().getFeePercentage();
                Transaction transactionPending = new Transaction(limit_buy.getAsset(),
                        priceExcludingFee,
                        transactionAssetAmount,
                        LocalDateTime.now(), transActionfee, limit_buy.getBuyer(), limit_sellMatch.getSeller());
                if (validateTransAction(transactionPending)) {
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

    private void processOrders(Map<Limit_Buy, List<Limit_Sell>> matchedOnLimitBuy) {
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
    }

    private boolean validateTransAction(Transaction transaction) {
        boolean transActionIsValid = transaction.getSellerWallet().sufficientAsset(transaction.getAsset(), transaction.getAssetAmount());
        if (BigBangkApplicatie.getBank().getWallet().equals(transaction.getSellerWallet())) {
            if (!transaction.getBuyerWallet().sufficientBalance(transaction.getPriceExcludingFee() + transaction.getFee())) {
                return false;
            }
        } else {
            if (!transaction.getBuyerWallet().sufficientBalance(transaction.getPriceExcludingFee() + transaction.getFee() / 2)) {
                return false;
            }
        }
        return transActionIsValid;
    }

    private void processTransaction(Transaction transaction) {
        Wallet buyerWallet = transaction.getBuyerWallet();
        Wallet sellerWallet = transaction.getSellerWallet();
        Wallet bankWallet = BigBangkApplicatie.getBank().getWallet();
        buyerWallet.removeFromBalance(transaction.getPriceExcludingFee());
        sellerWallet.addToBalance(transaction.getPriceExcludingFee());
        buyerWallet.addToAsset(transaction.getAsset(), transaction.getAssetAmount());
        sellerWallet.removeFromAsset(transaction.getAsset(), transaction.getAssetAmount());
        bankWallet.addToBalance(transaction.getFee());
        if (transaction.getBuyerWallet().equals(bankWallet)) {
            transaction.getSellerWallet().removeFromBalance(transaction.getFee());
        } else if (transaction.getSellerWallet().equals(bankWallet)) {
            transaction.getBuyerWallet().removeFromBalance(transaction.getFee());
        } else {
            transaction.getSellerWallet().removeFromBalance(transaction.getFee() / 2.0);
            transaction.getBuyerWallet().removeFromBalance(transaction.getFee() / 2.0);
        }
        rootRepository.saveTransaction(transaction);
    }

    private static class lowestPriceThenOldest implements Comparator<Limit_Sell> {
        @Override
        public int compare(Limit_Sell o1, Limit_Sell o2) {
            return (o1.getOrderLimit() - o2.getOrderLimit()) == 0 ? o1.getDate().compareTo(o2.getDate()) :
                    (o1.getOrderLimit() - o2.getOrderLimit()) > 0 ? 1 : -1;
        }
    }
}

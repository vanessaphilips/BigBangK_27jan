// Created by Deek
// Creation date 1/18/2022

package com.example.project_bigbangk.service;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Orders.*;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
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


    //public List<AbstractOrder> checkForTriggered

    //limit buy ik wil kopen voor een bedrag niet hoger dan limit
    //limit sell ik wil kopen voor een bedrag niet lager dan limit
    public void checkForMatchingOrders() {
        List<Limit_Sell> allLimit_SellOrders = rootRepository.getAllLimitSell();
        List<Limit_Buy> allLimit_BuyOrders = rootRepository.getAllLimitBuy();

        for (Limit_Buy limit_buy : allLimit_BuyOrders) {
            double requestedPricePerAsset = limit_buy.getOrderLimit() / limit_buy.getAssetAmount();
            List<Limit_Sell> matches = allLimit_SellOrders.stream()
                    .filter(lso -> requestedPricePerAsset > lso.getOrderLimit() / lso.getAssetAmount())
                    .sorted(Comparator.comparing(AbstractOrder::getOrderLimit).reversed().thenComparing(AbstractOrder::getDate).reversed())
                    .collect(Collectors.toList());
            processMatches(limit_buy, matches);
        }
    }

    private void processMatches(Limit_Buy limit_buy, List<Limit_Sell> matches) {
        double amountOfAssets = limit_buy.getAssetAmount();
        int indexLimitSell = 0;
        while (amountOfAssets > 0 && indexLimitSell < matches.size()) {

            Limit_Sell limit_sellMatch = matches.get(indexLimitSell);
            double amountOfAssetsLimitSell = limit_sellMatch.getAssetAmount();
            double transactionAmountOfAssets = amountOfAssets - amountOfAssetsLimitSell < 0 ? amountOfAssets : amountOfAssetsLimitSell;
            double transActionfee = transactionAmountOfAssets * limit_sellMatch.getOrderLimit() / limit_sellMatch.getAssetAmount();
            processTransaction(new Transaction(limit_buy.getAsset(),
                    limit_sellMatch.getOrderLimit(),
                    transactionAmountOfAssets,
                    LocalDateTime.now(), transActionfee, limit_buy.getBuyer(), limit_sellMatch.getSeller()
            ));
        }
    }

    private void processTransaction(Transaction transaction) {
        Wallet buyerWallet = transaction.getBuyerWallet();
        Wallet sellerWallet = transaction.getSellerWallet();
        Wallet bankWallet = BigBangkApplicatie.bigBangk.getWallet();
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


}
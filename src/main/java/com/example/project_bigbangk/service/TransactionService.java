// Created by Deek
// Creation date 1/21/2022

package com.example.project_bigbangk.service;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Bank;
import com.example.project_bigbangk.model.Orders.Transaction;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final Logger logger = LoggerFactory.getLogger(TransactionService.class);
    Bank bigBangk = BigBangkApplicatie.bigBangk;
    RootRepository rootRepository;

    public TransactionService(RootRepository rootRepository) {
        super();
        logger.info("New TransactionService");
        this.rootRepository = rootRepository;
    }

    public boolean validateTransAction(Transaction transaction) {

        boolean transActionIsValid = transaction.getSellerWallet().sufficientAsset(transaction.getAsset(), transaction.getAssetAmount());
        if (bigBangk.getWallet().equals(transaction.getSellerWallet())) {
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

    public void processTransaction(Transaction transaction) {

        Wallet buyerWallet = transaction.getBuyerWallet();
        Wallet sellerWallet = transaction.getSellerWallet();
        Wallet bankWallet = bigBangk.getWallet();
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
            transaction.getSellerWallet().removeFromBalance(transaction.getFee() );
            transaction.getBuyerWallet().removeFromBalance(transaction.getFee());
        }
        rootRepository.saveTransaction(transaction);
    }
}
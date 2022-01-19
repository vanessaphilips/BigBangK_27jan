package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Orders.TransactionType;
import com.example.project_bigbangk.model.Wallet;
import org.springframework.dao.DataAccessException;

public interface IWalletDAO {

    void saveNewWallet(Wallet wallet);

    void updateBalance(Wallet wallet);

    Wallet findWalletByIban(String iban);

    void updateWalletAssets(Wallet wallet, Asset asset, double amount);

    void createWalletAsset(Wallet wallet, Asset asset, double amount);

    Double findAmountOfAsset(String iban, String assetCode);

    Wallet findWalletByEmail(String email);

    Wallet findWalletByBankCode(String bankCode);

    Wallet findWalletByOrderId(int orderId);

    Wallet FindSellerWalletByOrderId(int orderId);

    Wallet FindBuyerWalletByOrderId(int orderId);

}

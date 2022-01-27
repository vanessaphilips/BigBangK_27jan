package com.example.project_bigbangk.service;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.AssetCode_Name;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.GraphicsWalletDTO;
import com.example.project_bigbangk.model.DTO.WalletDTO;
import com.example.project_bigbangk.model.Orders.Transaction;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @Author Kelly Speelman - de Jonge
 */

@Service
public class WalletService {
    private final AuthenticateService authenticateService;
    private final RootRepository rootRepository;
    private Client client;

    @Autowired
    public WalletService (AuthenticateService authenticateService, RootRepository rootRepository) {
        super();
        this.authenticateService = authenticateService;
        this.rootRepository = rootRepository;
    }

    public Wallet getWallet(String token){
        this.client = authenticateService.getClientFromToken(token);
        return client.getWallet();
    }

    public WalletDTO getWalletDTO(String token){
        this.client = authenticateService.getClientFromToken(token);
        double totalWorth = client.getWallet().getBalance();
        for (Asset asset : client.getWallet().getAssets().keySet()) {
            totalWorth+= client.getWallet().getAssets().get(asset) * rootRepository.getCurrentPriceByAssetCode(asset.getCode());
        }
        double freeBalance = client.getWallet().freeBalance();
        return new WalletDTO(client.getWallet().getIban(), client.getWallet().getBalance(), client.getWallet().getAssets(), totalWorth, freeBalance);
    }


    public List<GraphicsWalletDTO> getWalletHistoryClient(String token){
        this.client = authenticateService.getClientFromToken(token);
        rootRepository.fillWalletWithTransactions(client);
        return calculateHisory();
    }

    public List<GraphicsWalletDTO> calculateHisory(){
        // todo Zorgen dat de asset juist word opgelagen
        List<GraphicsWalletDTO> history = new ArrayList<>();
        Map<Asset, Double> assetMap = new HashMap<>();
        for (AssetCode_Name asset : EnumSet.allOf(AssetCode_Name.class)) {
            assetMap.put(new Asset(asset.getAssetCode(), asset.getAssetName()), 0.0);
        }

        Wallet walletStart = new Wallet(this.client.getWallet().getIban(), BigBangkApplicatie.bigBangk.getStartingcapital(), assetMap);
        history.add(new GraphicsWalletDTO(walletStart, LocalDateTime.now().toString()));
        for (Transaction transaction : this.client.getWallet().getTransaction()) {
            Wallet wallet = history.get(history.size()-1).nieuw.clone();
            Asset asset = transaction.getAsset();
            double amount = transaction.getAssetAmount();
            if(transaction.getBuyerWallet().equals(client.getWallet())) {
                wallet.removeFromBalance(transaction.getPriceExcludingFee());
                wallet.addToAsset(asset, amount);
            } else {
                wallet.addToBalance(transaction.getPriceExcludingFee());
                wallet.removeFromAsset(asset, amount);
            }
            wallet.removeFromBalance(transaction.getFee());
            history.add(new GraphicsWalletDTO(wallet, transaction.getDate().toString()));
        }
        return history;
    }
}

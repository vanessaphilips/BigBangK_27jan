package com.example.project_bigbangk.service;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.DTO.GraphicsWalletDTO;
import com.example.project_bigbangk.model.Orders.Transaction;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.repository.RootRepository;
import com.example.project_bigbangk.service.Security.AuthenticateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Hier wordt de WalletService gestest.
 *
 * @Author Kelly Speelman - de Jonge
 */

class WalletServiceTest {
    private WalletService walletService;

    private final RootRepository rootRepository = Mockito.mock(RootRepository.class);
    private final AuthenticateService authenticateService = Mockito.mock(AuthenticateService.class);
    private final Client mockClient = Mockito.mock(Client.class);
    private final Wallet mockWallet = Mockito.mock(Wallet.class);
    private final Wallet mockWallet2 = Mockito.mock(Wallet.class);
    private final Asset mockAssetCardano = Mockito.mock(Asset.class);
    private final Asset mockAssetDai = Mockito.mock(Asset.class);
    private final Asset mockAssetLitecoin = Mockito.mock(Asset.class);
    private final Transaction transaction1 = Mockito.mock(Transaction.class);
    private final Transaction transaction2 = Mockito.mock(Transaction.class);
    private final Transaction transaction3 = Mockito.mock(Transaction.class);

    @BeforeEach
    void setUp(){
        this.walletService = new WalletService(authenticateService, rootRepository);

        Mockito.when(mockWallet.getIban()).thenReturn("NL17 BGBK 7265515");
        Mockito.when(mockWallet.getBalance()).thenReturn(1000.0);

        Mockito.when(mockAssetCardano.getName()).thenReturn("Cardano");
        Mockito.when(mockAssetCardano.getCode()).thenReturn("ADA");
        Mockito.when(mockAssetDai.getName()).thenReturn("Dai");
        Mockito.when(mockAssetDai.getCode()).thenReturn("DAI");
        Mockito.when(mockAssetLitecoin.getName()).thenReturn("Litecoin");
        Mockito.when(mockAssetLitecoin.getCode()).thenReturn("LTC");
        Map<Asset, Double> assetMap = new HashMap<>();
        assetMap.put(mockAssetCardano, 4.0);
        assetMap.put(mockAssetDai, 0.0);
        assetMap.put(mockAssetLitecoin, 0.0);

        Mockito.when(mockWallet.getAssets()).thenReturn(assetMap);
        Asset asset = new Asset("DAI", "Dai");

        Mockito.when(transaction1.getAsset()).thenReturn(asset);
        Mockito.when(transaction1.getPriceExcludingFee()).thenReturn(240.0);
        Mockito.when(transaction1.getAssetAmount()).thenReturn(2.0);
        Mockito.when(transaction1.getFee()).thenReturn(2.0);
        Mockito.when(transaction1.getDate()).thenReturn(LocalDateTime.now().minusDays(3));
        Mockito.when(transaction1.getBuyerWallet()).thenReturn(mockWallet);

        Mockito.when(transaction2.getAsset()).thenReturn(asset);
        Mockito.when(transaction2.getPriceExcludingFee()).thenReturn(600.0);
        Mockito.when(transaction2.getAssetAmount()).thenReturn(2.0);
        Mockito.when(transaction2.getFee()).thenReturn(4.0);
        Mockito.when(transaction2.getDate()).thenReturn(LocalDateTime.now().minusDays(3).minusHours(7));
        Mockito.when(transaction2.getBuyerWallet()).thenReturn(mockWallet);

        Mockito.when(transaction3.getAsset()).thenReturn(asset);
        Mockito.when(transaction3.getPriceExcludingFee()).thenReturn(670.0);
        Mockito.when(transaction3.getAssetAmount()).thenReturn(3.0);
        Mockito.when(transaction3.getFee()).thenReturn(4.5);
        Mockito.when(transaction3.getDate()).thenReturn(LocalDateTime.now().minusDays(3).minusHours(7));
        Mockito.when(transaction3.getBuyerWallet()).thenReturn(mockWallet2);
        Mockito.when(transaction3.getSellerWallet()).thenReturn(mockWallet);

        Mockito.when(mockClient.getWallet()).thenReturn(mockWallet);

        Mockito.when(authenticateService.getClientFromToken("token")).thenReturn(mockClient);
    }

    @Test
    void getWalletClient() {
        Wallet klantWallet = walletService.getWalletClient("token");
        assertThat(klantWallet).isEqualTo(mockWallet);
    }

    @Test
    void getWalletHistoryClient() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction1);
        transactions.add(transaction2);
        transactions.add(transaction3);
        Mockito.when(mockWallet.getTransaction()).thenReturn(transactions);
        Mockito.when(walletService.getWalletClient("token")).thenReturn(mockWallet);
        List<GraphicsWalletDTO> history = walletService.calculateHisory();
        System.out.println(history.get(0).nieuw);
        System.out.println(history.get(1).nieuw);
        System.out.println(history.get(2).nieuw);
        System.out.println(history.get(3).nieuw);
        assertThat(history.get(1).nieuw).as("eerste vergelijking").isNotEqualTo(history.get(2).nieuw).isNotEqualTo(history.get(3).nieuw);
        assertThat(history.get(2).nieuw).as("tweede vergelijking").isNotEqualTo(history.get(3).nieuw);
        assertThat(history.get(1).nieuw.getBalance()).as("test de eerste history balance").isEqualTo(9758.0);
        assertThat(history.get(2).nieuw.getBalance()).as("test de tweede history balance").isEqualTo(9154.0);
        assertThat(history.get(3).nieuw.getBalance()).as("test de derde history balance").isEqualTo(9819.5);
        assertThat(history.get(1).dateTime).isEqualTo(transaction1.getDate().toString());
        assertThat(history.get(2).dateTime).isEqualTo(transaction2.getDate().toString());
        assertThat(history.get(3).dateTime).isEqualTo(transaction3.getDate().toString());
        // todo nog werkend maken
        /*
        assertThat(history.get(1).nieuw.getAssets().get(transaction1.getAsset())).as("test de eerste assets").isEqualTo(2.0);
        assertThat(history.get(2).nieuw.getAssets().get(transaction2.getAsset())).as("test de tweede assets").isEqualTo(4.0);
        assertThat(history.get(3).nieuw.getAssets().get(transaction3.getAsset())).as("test de tweede assets").isEqualTo(1.0);*/

    }

    @Test
    void calculateWalletBalanceHistory() {

    }

    @Test
    void calculateCurrentValue() {
    }
}
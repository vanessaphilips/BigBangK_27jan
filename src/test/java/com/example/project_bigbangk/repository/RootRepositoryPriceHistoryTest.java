package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.AssetCode_Name;
import com.example.project_bigbangk.model.PriceDate;
import com.example.project_bigbangk.model.PriceHistory;
import com.example.project_bigbangk.service.OrderMatchingService;
import com.example.project_bigbangk.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Rootrepository test voor de PriceHistory sectie
 *
 * @author Pieter jan Bleichrodt
 */
@SpringBootTest
class RootRepositoryPriceHistoryTest {

    @MockBean
    OrderMatchingService orderMatchingService;
    @Resource
    private RootRepository rootRepository;

    @MockBean
    WalletService walletService;


    @MockBean
    private IPricedateDAO priceDateDAO;
    @MockBean
    private IAssetDAO jdbcAssetDAO;

    private List<Asset> assets = new ArrayList<>();

    private LocalDateTime localDateTime;

    @BeforeEach
    public void setup() {
        localDateTime = LocalDateTime.now().minusDays(1);
        List<PriceDate> priceDates = createPriceDates(Arrays.asList(0.7, 1.3, 1.1, 1.21));
        createAsset(AssetCode_Name.BTC);
        when(priceDateDAO.getPriceDatesByCodeFromDate(localDateTime, AssetCode_Name.BTC.getAssetCode())).thenReturn(priceDates);
        priceDates = createPriceDates(Arrays.asList(1.3, 4.3, 2.3, 6.3));
        createAsset(AssetCode_Name.ETH);
        when(priceDateDAO.getPriceDatesByCodeFromDate(localDateTime, AssetCode_Name.ETH.getAssetCode())).thenReturn(priceDates);
        priceDates = createPriceDates(Arrays.asList(1.5, 5.5, 2.5, 0.5));
        createAsset(AssetCode_Name.ADA);
        when(priceDateDAO.getPriceDatesByCodeFromDate(localDateTime, AssetCode_Name.ADA.getAssetCode())).thenReturn(priceDates);
    }


    @Test
    void getAllPriceHistroriesByAssets() {
        when(jdbcAssetDAO.getAllAssets()).thenReturn(assets);
        List<PriceHistory> priceHistories = rootRepository.getAllPriceHistories(localDateTime);
        assertEquals(3, priceHistories.size());
    }

    @Test
    void getAllPriceHistroriesByAssets2() {
        when(jdbcAssetDAO.getAllAssets()).thenReturn(null);
        List<PriceHistory> priceHistories = rootRepository.getAllPriceHistories(localDateTime);
        assertNull(priceHistories);
    }


    private List<PriceDate> createPriceDates(List<Double> currentPrices) {
        List<PriceDate> mockedPriceDates = new ArrayList<>();
        for (double currentPrice : currentPrices) {
            PriceDate priceDate = mock(PriceDate.class);
            when(priceDate.getPrice()).thenReturn(currentPrice);
            //Do a Thread sleep to make distinct localDateTimes
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            when(priceDate.getDateTime()).thenReturn(LocalDateTime.now());
            mockedPriceDates.add(priceDate);
        }
        return mockedPriceDates;
    }

    private Asset createAsset(AssetCode_Name assetCodeName) {
        Asset asset = mock(Asset.class);
        when(asset.getName()).thenReturn(assetCodeName.getAssetName());
        when(asset.getCode()).thenReturn(assetCodeName.getAssetCode());
        when(asset.getCurrentPrice()).thenReturn(1.0);
        assets.add(asset);
        return asset;
    }

}
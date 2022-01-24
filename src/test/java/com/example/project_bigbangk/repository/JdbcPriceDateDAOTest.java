package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.BigBangkApplicatie;
import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.AssetCode_Name;
import com.example.project_bigbangk.model.PriceDate;
import com.example.project_bigbangk.model.PriceHistory;
import com.example.project_bigbangk.service.OrderMatchingService;
import com.example.project_bigbangk.service.priceHistoryUpdate.PriceHistoryUpdateService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.jdbc.init.DataSourceScriptDatabaseInitializer;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)

@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class JdbcPriceDateDAOTest {
    @MockBean
    OrderMatchingService orderMatchingService;
@MockBean
    BigBangkApplicatie bigBangkApplicatie;
    @Resource
    JdbcPriceDateDAO priceDateDAO;
    @MockBean
    PriceHistoryUpdateService priceHistoryUpdateService;
    @MockBean
    JdbcClientDAO jdbcClientDAO;
    @MockBean
    JdbcAddressDAO jdbcAdressDAO;
    @MockBean
    RootRepository rootRepository;
    @MockBean
    JdbcAssetDAO jdbcAssetDAO;



    private List<Asset> assets = new ArrayList<>();
    private List<PriceHistory> priceHistories = new ArrayList<>();


    private List<PriceDate> createPriceDates(List<Double> currentPrices) {
        List<PriceDate> priceDates = new ArrayList<>();
        for (double currentPrice : currentPrices) {
                       PriceDate priceDate = Mockito.mock(PriceDate.class);
            Mockito.when(priceDate.getPrice()).thenReturn(currentPrice);
            //Do a Thread sleep to make distinct localDateTimes
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Mockito.when(priceDate.getDateTime()).thenReturn(LocalDateTime.now());
            priceDates.add(priceDate);
        }
        return priceDates;
    }

    @Test
    @Order(1)
    void savePriceDates() {
        savePriceDates(createPriceDates(Arrays.asList(0.7, 1.3, 1.1, 1.21)), AssetCode_Name.BTC);
        savePriceDates(createPriceDates(Arrays.asList(1.3, 4.3, 2.3, 6.3)), AssetCode_Name.ETH);
        savePriceDates(createPriceDates(Arrays.asList(1.5, 5.5, 2.5, 0.5)), AssetCode_Name.ADA);
    }

    private void savePriceDates(List<PriceDate> priceDates, AssetCode_Name assetCodeName) {
        for (PriceDate priceDate : priceDates) {
            priceDateDAO.savePriceDate(priceDate, assetCodeName.getAssetCode());
        }
    }

    @Test
    @Order(2)
    void getCurrentPriceByAssetCode() {

        double actual = priceDateDAO.getCurrentPriceByAssetCode(AssetCode_Name.BTC.getAssetCode());
        double expected = 1.21;
        assertEquals(expected, actual);
        actual = priceDateDAO.getCurrentPriceByAssetCode(AssetCode_Name.ETH.getAssetCode());
        expected = 6.3;
        assertEquals(expected, actual);
        //find asset with no PriceHistory
        actual = priceDateDAO.getCurrentPriceByAssetCode(AssetCode_Name.BUSD.getAssetCode());
        expected = -1;
        assertEquals(expected, actual);
        //find asset that doesn't exist
        actual = priceDateDAO.getCurrentPriceByAssetCode("SDFSS");
        assertEquals(expected, actual);
    }

    @Test
    @Order(3)
    void getAllPriceHistory() {
        List<PriceDate> actual = priceDateDAO.getPriceDatesByCodeFromDate(LocalDateTime.now().minusYears(20), "BTC");
        System.out.println(actual);
        assertThat(actual.size()).isEqualTo(9);
        actual = priceDateDAO.getPriceDatesByCodeFromDate(LocalDateTime.now().minusSeconds(20), "BTC");
        assertThat(actual.size()).isEqualTo(4);
        //find asset with no PriceHistory
        actual = priceDateDAO.getPriceDatesByCodeFromDate(LocalDateTime.now().minusSeconds(20), "BIC");
        assertNull(actual);
        //find asset that doesn't exist
        actual = priceDateDAO.getPriceDatesByCodeFromDate(LocalDateTime.now().minusSeconds(20), "asfsg");
        assertNull(actual);
    }
}
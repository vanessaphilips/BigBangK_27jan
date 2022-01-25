package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.PriceDate;
import com.example.project_bigbangk.model.PriceHistory;

import java.time.LocalDateTime;
import java.util.List;

public interface IPricedateDAO {

    void savePriceDate(PriceDate priceDate, String assetCode);

    double getCurrentPriceByAssetCode(String assetCode);

    List<PriceDate> getPriceDatesByCodeFromDate(LocalDateTime date, String assetCode);

    Double getPriceDateByCodeOnDate(LocalDateTime date, String assetCode);
}

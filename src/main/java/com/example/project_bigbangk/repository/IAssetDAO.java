package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Asset;

import java.util.List;

public interface IAssetDAO {
    List<Asset> getAllAssets();

    void saveAsset(Asset asset);

    int getNumberOfAssets();

    Asset findAssetByCode(String assetCode);

    Asset findAssetByOrderId(int orderId);
}

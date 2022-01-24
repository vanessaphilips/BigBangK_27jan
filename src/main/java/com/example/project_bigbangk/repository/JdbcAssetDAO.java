// Created by Deek
// Creation date 12/14/2021

package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.AssetCode_Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class JdbcAssetDAO implements IAssetDAO {

    private final Logger logger = LoggerFactory.getLogger(JdbcAssetDAO.class);
    JdbcTemplate jdbcTemplate;

    public JdbcAssetDAO(JdbcTemplate jdbcTemplate) {
        super();
        logger.info("New JdbcAssetDAO");
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Asset> getAllAssets() {
        String sql = "Select * from asset;";
        List<Asset> assets = new ArrayList<>();
        try {
           assets = jdbcTemplate.query(sql, new AssetRowMapper());
        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
        return assets;
    }

    @Override
    public void saveAsset(Asset asset) {
        String sql = "Insert into asset values(?,?);";
        try {
            jdbcTemplate.update(sql, asset.getCode(),
                    asset.getName());
        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
    }

    /**
     * for checking the numberOfassets already in DB
     *
     * @return number of rows from Asset Table
     */
    @Override
    public int getNumberOfAssets() {
        String sql = "SELECT count(code) FROM asset";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class);

        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
        return 0;
    }

    @Override
    public Asset findAssetByCode(String assetCode) {
        String slq = "SELECT * FROM asset WHERE code = ?;";
        Asset asset = null;
        try {
            asset = jdbcTemplate.queryForObject(slq, new AssetRowMapper(), assetCode);
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return asset;
    }

    @Override
    public Asset findAssetByOrderId(int orderId) {
        String slq = "SELECT a.* FROM asset a JOIN bigbangk.order o ON a.code = o.assetCode  WHERE orderId = ?;";
        Asset asset = null;
        try {
            asset = jdbcTemplate.queryForObject(slq, new AssetRowMapper(), orderId);
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return asset;
    }

    private class AssetRowMapper implements RowMapper<Asset> {
        @Override
        public Asset mapRow(ResultSet resultSet, int rowNumber) throws SQLException {

            return new Asset(resultSet.getString("code"), resultSet.getString("name"));
        }
    }
}

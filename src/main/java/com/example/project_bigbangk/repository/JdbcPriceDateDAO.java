// Created by Deek
// Creation date 12/14/2021

package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.PriceDate;
import com.example.project_bigbangk.model.PriceHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class JdbcPriceDateDAO implements IPricedateDAO {

    private final Logger logger = LoggerFactory.getLogger(JdbcPriceDateDAO.class);
    JdbcTemplate jdbcTemplate;

    public JdbcPriceDateDAO(JdbcTemplate jdbcTemplate) {
        super();
        logger.info("New JdbcPriceDateDAO");
        this.jdbcTemplate = jdbcTemplate;
    }




    @Override
    public void savePriceDate(PriceDate priceDate, String assetCode) {
        String sql = "Insert into PriceHistory values(?,?,?);";
                    try {
                jdbcTemplate.update(sql,
                        priceDate.getDateTime(),
                        assetCode,
                        priceDate.getPrice());
            } catch (DataAccessException dataAccessException) {
                logger.info(dataAccessException.getMessage());
            }
       }


    @Override
    public double getCurrentPriceByAssetCode(String assetCode) {
        String sql = "Select * from (SELECT * FROM pricehistory where code = ? )as priceHistoryByCoin ORDER BY dateTime DESC LIMIT 1;";
        double currentPrice = -1;
        try {
            PriceDate priceDate = jdbcTemplate.queryForObject(sql,
                    new PriceDateRowMapper(), assetCode);
            currentPrice = priceDate == null ? -1 : priceDate.getPrice();
        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
        return currentPrice;
    }

    @Override
    public List<PriceDate> getPriceDatesByCodeFromDate(LocalDateTime date, String assetCode) {
        String sql = "SELECT * FROM pricehistory where dateTime> ? and code = ?;";
        List<PriceDate> priceDates = null;
        try {
            priceDates = jdbcTemplate.query(sql, new PriceDateRowMapper(), date, assetCode);
            if (priceDates.size() == 0) {
                priceDates = null;
            }
        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
        return priceDates;
    }

    @Override
    public Double getPriceDateByCodeOnDate(LocalDateTime date, String assetCode) {
        String sql = "SELECT * FROM pricehistory where dateTime > ? and datTime < ? and code = ?;";
        List<PriceDate> priceDates = null;
        LocalDateTime eerder = date.minusMinutes(2).minusSeconds(6);
        LocalDateTime later = date.plusMinutes(2).plusSeconds(5);
        try {
            priceDates = jdbcTemplate.query(sql, new PriceDateRowMapper(), eerder, later, assetCode);
            if (priceDates.size() == 0) {
                priceDates = null;
            }
        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
        return priceDates.get(0).getPrice();
    }

    private class PriceDateRowMapper implements RowMapper<PriceDate> {
        @Override
        public PriceDate mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
            LocalDate localDate = resultSet.getDate("datetime").toLocalDate();
            LocalDateTime localDateTime = localDate.atTime(resultSet.getTime("datetime").toLocalTime());
            return new PriceDate(localDateTime, resultSet.getDouble("price"));
        }
    }
}
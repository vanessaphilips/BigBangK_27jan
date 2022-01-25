

package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.PriceDate;
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

/**
 * DAO for the priceDate/PriceHistory class
 * @author Pieter Jan BLeichrodt
 */
@Repository
public class JdbcPriceDateDAO implements IPricedateDAO {

    private final Logger logger = LoggerFactory.getLogger(JdbcPriceDateDAO.class);
    JdbcTemplate jdbcTemplate;

    public JdbcPriceDateDAO(JdbcTemplate jdbcTemplate) {
        super();
        logger.info("New JdbcPriceDateDAO");
        this.jdbcTemplate = jdbcTemplate;
    }


    /**
     * saves a single priceDate to the database
     * @param priceDate the priceDate to be saved
     * @param assetCode the code of the asset in string format
     */
    @Override
    public void savePriceDate(PriceDate priceDate, String assetCode) {
        String sql = "Insert into pricehistory values(?,?,?);";
                    try {
                jdbcTemplate.update(sql,
                        priceDate.getDateTime(),
                        assetCode,
                        priceDate.getPrice());
            } catch (DataAccessException dataAccessException) {
                logger.info(dataAccessException.getMessage());
            }
       }

    /**
     * retrieves the currentPrice of a given Asset from the database     *
     * @param assetCode the code of the asset in string format for which the currentPrice is returned
     * @return the currentPrice as a double
     */
    @Override
    public double getCurrentPriceByAssetCode(String assetCode) {
        String sql = "Select * from (SELECT * FROM pricehistory where code = ? )as pricehistorybycoin ORDER BY datetime DESC LIMIT 1;";
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

    /**
     * retrieves the pricehistory of a given Asset from the database
     * @param assetCode the code of the asset in string format for which the pricehistory is returned
     * @param date date in past for defining the interval for which priceData is retrieved
     * @return the currentPrice as a double
     */
    @Override
    public List<PriceDate> getPriceDatesByCodeFromDate(LocalDateTime date, String assetCode) {
        String sql = "SELECT * FROM pricehistory where datetime> ? and code = ?;";
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
        String sql = "SELECT * FROM pricehistory where datetime > ? and datetime < ? and code = ?;";
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
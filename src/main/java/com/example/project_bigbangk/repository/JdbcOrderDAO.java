package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Orders.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * Created by Vanessa Philips.
 * DAO for orders, typified by "Transaction", "Limit_sell", "Limit_buy", "Stoploss_sell".
 */

@Repository
public class JdbcOrderDAO {

    private final Logger logger = LoggerFactory.getLogger(JdbcOrderDAO.class);
    JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcOrderDAO(JdbcTemplate jdbcTemplate) {
        super();
        this.jdbcTemplate = jdbcTemplate;
        logger.info("New JdbcOrderDAO");
    }

    //Transaction
    public void saveTransaction(Transaction transaction){
        String sql = "INSERT INTO bigbangk.order (buyer, seller, assetCode, orderType, assetAmount, date, fee, priceExcludingFee) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";

        try {
            jdbcTemplate.update(sql,
                    transaction.getBuyerWallet().getIban(),
                    transaction.getSellerWallet().getIban(),
                    transaction.getAsset().getCode(),
                    TransactionType.TRANSACTION.toString(),
                    transaction.getAssetAmount(),
                    java.sql.Timestamp.valueOf(transaction.getDate()),
                    transaction.getFee(),
                    transaction.getPriceExcludingFee());

        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
    }

    public Transaction findTransactionById(Long orderId){
        String sql = "SELECT * FROM Order WHERE id=?;";
        try {
            return jdbcTemplate.queryForObject(sql, new TransactionRowMapper(), orderId);
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return null;
    }

    private static class TransactionRowMapper implements RowMapper<Transaction> {

        @Override
        public Transaction mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            int orderId = resultSet.getInt("orderId");
            Double priceExcludingFee = resultSet.getDouble("priceExcludingFee");
            Integer assetAmount = resultSet.getInt("assetAmount");
            LocalDateTime date = resultSet.getObject("date", LocalDateTime.class);
            Double fee = resultSet.getDouble("fee");
            return new Transaction(orderId, priceExcludingFee, assetAmount, date, fee);
        }
    }
    private static class LimitSellRowMapper implements RowMapper<Limit_Sell> {

        @Override
        public Limit_Sell mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            int orderId = resultSet.getInt("orderId");
            Double requestedPrice = resultSet.getDouble("orderLimit");
            Integer assetAmount = resultSet.getInt("assetAmount");
            LocalDateTime date = resultSet.getObject("date", LocalDateTime.class);
            return new Limit_Sell(orderId, requestedPrice, assetAmount, date);
        }
    }

    private static class LimitBuyRowMapper implements RowMapper<Limit_Buy> {

        @Override
        public Limit_Buy mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            int orderId = resultSet.getInt("orderId");
            Double limit = resultSet.getDouble("orderLimit");
            Integer assetAmount = resultSet.getInt("assetAmount");
            LocalDateTime date = resultSet.getObject("date", LocalDateTime.class);
            return new Limit_Buy(orderId, limit, assetAmount, date);
        }
    }
    private static class StopLossRowMapper implements RowMapper<Stoploss_Sell> {

        @Override
        public Stoploss_Sell mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            int orderId = resultSet.getInt("orderId");
            Double limit = resultSet.getDouble("orderLimit");
            Integer assetAmount = resultSet.getInt("assetAmount");
            LocalDateTime date = resultSet.getObject("date", LocalDateTime.class);

            return new Stoploss_Sell(orderId,  limit, assetAmount, LocalDateTime.now());
        }
    }


    /**
     * Saves Limit_Buy order in database, waiting to be matched with another client's offer (matchservice).
     * @param limit_buy
     * author = Vanessa Philips
     */
    public void saveLimit_Buy(Limit_Buy limit_buy){
        String sql = "INSERT INTO bigbangk.order (buyer, assetCode, orderType, orderlimit, assetAmount, date) VALUES (?, ?, ?, ?, ?, ?);";

        try {
            jdbcTemplate.update(sql,
                    limit_buy.getBuyer().getIban(),
                    limit_buy.getAsset().getCode(),
                    TransactionType.LIMIT_BUY.toString(),
                    limit_buy.getOrderLimit(),
                    limit_buy.getAssetAmount(),
                    java.sql.Timestamp.valueOf(limit_buy.getDate()));
        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
    }


    /**
     * Saves Limit_Sell order in database, waiting to be matched with another client's offer (matchservice).
     * @param limit_sell
     * author = Vanessa Philips
     */
    public void saveLimit_Sell(Limit_Sell limit_sell){
        String sql = "INSERT INTO bigbangk.order (seller, assetCode, orderType, orderlimit, assetAmount, date) VALUES (?, ?, ?, ?, ?, ?);";

        try {
            jdbcTemplate.update(sql,
                    limit_sell.getSeller().getIban(),
                    limit_sell.getAsset().getCode(),
                    TransactionType.LIMIT_SELL.toString(),
                    limit_sell.getOrderLimit(),
                    limit_sell.getAssetAmount(),
                    java.sql.Timestamp.valueOf(limit_sell.getDate()));
        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
    }



}
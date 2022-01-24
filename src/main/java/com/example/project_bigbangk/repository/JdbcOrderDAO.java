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
import java.util.List;

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
    public void saveTransaction(Transaction transaction) {
        String sql = "insert into bigbangk.order (buyer, seller, assetcode, ordertype, assetamount, date, fee, priceexcludingfee) values (?, ?, ?, ?, ?, ?, ?, ?);";

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

    public Transaction findTransactionById(Long orderId) {
        String sql = "SELECT * FROM bigbangk.order WHERE id=?;";
        try {
            return jdbcTemplate.queryForObject(sql, new TransactionRowMapper(), orderId);
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return null;
    }

    public List<Transaction> findAllTransactionsByIban(String iban) {
        String sql = "SELECT * FROM bigbangk.order WHERE ordertype = ? && buyer = ? || seller = ?;";
        try {
            return jdbcTemplate.query(sql, new TransactionRowMapper(), TransactionType.TRANSACTION.toString(), iban, iban);
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return null;
    }

    /**
     * Saves Limit_Buy order in database, waiting to be matched with another client's offer -> matchservice.
     *
     * @param limit_buy author = Vanessa Philips
     */
    public void saveLimit_Buy(Limit_Buy limit_buy) {
        String sql = "insert into bigbangk.order (buyer, assetcode, ordertype, orderlimit, assetamount, date) values (?, ?, ?, ?, ?, ?);";

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
     * Saves Limit_Sell order in database, waiting to be matched with another client's offer -> matchservice.
     *
     * @param limit_sell author = Vanessa Philips
     */
    public void saveLimit_Sell(Limit_Sell limit_sell) {
        String sql = "insert into bigbangk.order (seller, assetcode, ordertype, orderlimit, assetamount, date) values (?, ?, ?, ?, ?, ?);";

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

    /**
     * Saves Stoploss_Sell order in database, waiting to be matched with an offer (bank) -> matchservice.
     *
     * @param stoploss_sell author = Vanessa Philips
     */
    public void saveStoploss_Sell(Stoploss_Sell stoploss_sell) {
        String sql = "insert into bigbangk.order (seller, assetcode, ordertype, orderlimit, assetamount, date) values (?, ?, ?, ?, ?, ?);";

        try {
            jdbcTemplate.update(sql,
                    stoploss_sell.getSeller().getIban(),
                    stoploss_sell.getAsset().getCode(),
                    TransactionType.STOPLOSS_SELL.toString(),
                    stoploss_sell.getOrderLimit(),
                    stoploss_sell.getAssetAmount(),
                    java.sql.Timestamp.valueOf(stoploss_sell.getDate()));
        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
    }

    /**
     * for retreiving all Limit_buy orders
     *
     * @return List<Limit_Buy> with all limit_Buy orders
     */
    public List<Limit_Buy> getAllLimitBuys() {
        String sql = "SELECT * FROM bigbangk.order WHERE ordertype=?;";
        try {
            return jdbcTemplate.query(sql, new LimitBuyRowMapper(), TransactionType.LIMIT_BUY.toString());
        } catch (DataAccessException dataAccessException) {
            logger.info(dataAccessException.getMessage());
        }
        return null;
    }

    /**
     * for retreiving all Limit_Sell orders
     *
     * @return List<Limit_Sell> with all limit_Sell orders
     */
    public List<Limit_Sell> getAllLimitSells() {
        String sql = "SELECT * FROM bigbangk.order WHERE ordertype=?;";
        try {
            return jdbcTemplate.query(sql, new LimitSellRowMapper(), TransactionType.LIMIT_SELL.toString());
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return null;
    }

    /**
     * for retreiving all Stoploss_Sell orders
     *
     * @return List<Stoploss_Sell> with all Stoploss_Sell orders
     */
    public List<Stoploss_Sell> getAllStopLossSells() {
        String sql = "SELECT * FROM bigbangk.order WHERE ordertype=?;";
        try {
            return jdbcTemplate.query(sql, new StopLossRowMapper(), TransactionType.STOPLOSS_SELL.toString());
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return null;
    }

    /**
     * for deleting an order by Id
     *
     * @param orderId the id of the order to be deleted
     * @return return true if a row is affected, false when the order didn't exist
     */
    public boolean deleteOrderById(int orderId) {
        String sql = "DELETE FROM bigbangk.order where orderid=?;";
        try {
            return jdbcTemplate.update(sql, orderId) > 0;
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return false;
    }

    public boolean updateLimitSell(Limit_Sell limit_sell) {
        String sql = "UPDATE  bigbangk.order Set seller=?, assetamount=?, orderlimit=?, assetcode=? WHERE orderid= ?;";
        try {
            return jdbcTemplate.update(sql, limit_sell.getSeller().getIban(),
                    limit_sell.getAssetAmount(),
                    limit_sell.getOrderLimit(),
                    limit_sell.getAsset().getCode(),
                    limit_sell.getOrderId()) > 0;
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return false;
    }

    public boolean updateLimitBuy(Limit_Buy limit_buy) {
        String sql = "update bigbangk.order set seller=?, assetamount=?, orderlimit=?, assetcode=? where orderid= ?;";
        try {
            return jdbcTemplate.update(sql, limit_buy.getBuyer().getIban(),
                    limit_buy.getAssetAmount(),
                    limit_buy.getOrderLimit(),
                    limit_buy.getAsset().getCode(),
                    limit_buy.getOrderId()) > 0;
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return false;
    }

    public boolean updateStopLoss(Stoploss_Sell stoploss_sell) {
        String sql = "update bigbangk.order set seller=?, assetamount=?, orderlimit=?, assetcode=? where orderid= ?;";
        try {
            return jdbcTemplate.update(sql, stoploss_sell.getSeller().getIban(),
                    stoploss_sell.getAssetAmount(),
                    stoploss_sell.getOrderLimit(),
                    stoploss_sell.getAsset().getCode(),
                    stoploss_sell.getOrderId()) > 0;
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return false;
    }


    private static class TransactionRowMapper implements RowMapper<Transaction> {

        @Override
        public Transaction mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            int orderId = resultSet.getInt("orderid");
            double priceExcludingFee = resultSet.getDouble("priceexcludingfee");
            double assetAmount = resultSet.getDouble("assetamount");
            LocalDateTime date = resultSet.getObject("date", LocalDateTime.class);
            double fee = resultSet.getDouble("fee");
            return new Transaction(orderId, priceExcludingFee, assetAmount, date, fee);
        }
    }

    private static class LimitSellRowMapper implements RowMapper<Limit_Sell> {

        @Override
        public Limit_Sell mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return createOrderOfType(resultSet, TransactionType.LIMIT_SELL);
        }
    }

    private static class LimitBuyRowMapper implements RowMapper<Limit_Buy> {

        @Override
        public Limit_Buy mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return createOrderOfType(resultSet, TransactionType.LIMIT_BUY);
        }
    }

    private static class StopLossRowMapper implements RowMapper<Stoploss_Sell> {

        @Override
        public Stoploss_Sell mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return createOrderOfType(resultSet, TransactionType.STOPLOSS_SELL);
        }
    }

    static private <T> T createOrderOfType(ResultSet resultSet, TransactionType type) throws SQLException {
        int orderId = resultSet.getInt("orderid");
        double limit = resultSet.getDouble("orderlimit");
        double assetAmount = resultSet.getDouble("assetamount");
        LocalDateTime date = resultSet.getObject("date", LocalDateTime.class);
        switch (type) {
            case LIMIT_BUY:
                return (T) new Limit_Buy(orderId, limit, assetAmount, date);
            case LIMIT_SELL:
                return (T) new Limit_Sell(orderId, limit, assetAmount, date);
            case STOPLOSS_SELL:
                return (T) new Stoploss_Sell(orderId, limit, assetAmount, date);
        }
        return null;
    }

}
package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Asset;
import com.example.project_bigbangk.model.Orders.TransactionType;
import com.example.project_bigbangk.model.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.ResultSet;
import java.sql.SQLException;


@Repository
public class JdbcWalletDAO implements IWalletDAO{

    JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcWalletDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveNewWallet(Wallet wallet) {
        String slq = "Insert into wallet values(?,?);";
        jdbcTemplate.update(slq, wallet.getIban(), wallet.getBalance());
    }

    public void updateBalance(Wallet wallet) {
        String slq = "Update wallet Set balance = ? WHERE IBAN = ?";
        jdbcTemplate.update(slq, wallet.getBalance(), wallet.getIban());
    }

    public Wallet findWalletByIban(String iban) {
        String sql = "Select * From wallet Where IBAN = ?;";
        Wallet wallet = null;
        try {
            wallet = jdbcTemplate.queryForObject(sql, new walletRowMapper(), iban);
        } catch (DataAccessException dataAccessException) {
            if (! dataAccessException.getMessage().toString().equals("Incorrect result size: expected 1, actual 0")) {
            System.err.println(dataAccessException.getMessage());}
        }
        return wallet;
    }

    /**
     * finds the Wallet of the client that placed the order, only works for open orders, not transactions
     * @param orderId unique id of order
     * @return Wallet
     */
    public Wallet findWalletByOrderId(int orderId){
        String sql = String.format("Select * From wallet JOIN `order` ON `order`.buyer = wallet.IBAN " +
                "UNION Select * From wallet JOIN `order` ON `order`.seller = wallet.IBAN" +
                "WHERE orderId = ? AND NOT orderType = '%s';", TransactionType.TRANSACTION.toString());
        Wallet wallet = null;
        try {
            wallet = jdbcTemplate.queryForObject(sql, new walletRowMapper(), orderId);
        } catch (DataAccessException dataAccessException) {
            if (! dataAccessException.getMessage().toString().equals("findWalletByOrderId: Incorrect result size: expected 1, actual 0")) {
                System.err.println(dataAccessException.getMessage());}
        }
        return wallet;
    }

    /**
     * finds the Wallet of the Seller, only for use with 'transactions'
     * @param orderId unique id of order
     * @return Wallet
     */
    public Wallet FindSellerWalletByOrderId(int orderId){
        String sql = String.format("Select * From wallet JOIN `order` ON `order`.seller = wallet.IBAN" +
                "WHERE orderId = ? AND orderType = '%s';", TransactionType.TRANSACTION.toString());
        Wallet wallet = null;
        try {
            wallet = jdbcTemplate.queryForObject(sql, new walletRowMapper(), orderId);
        } catch (DataAccessException dataAccessException) {
            if (! dataAccessException.getMessage().toString().equals("FindSellerWalletByOrderID: Incorrect result size: expected 1, actual 0")) {
                System.err.println(dataAccessException.getMessage());}
        }
        return wallet;
    }

    /**
     * finds the Wallet of the Buyer, only for use with 'transactions'
     * @param orderId unique id of order
     * @return Wallet
     */
    public Wallet FindBuyerWalletByOrderId(int orderId){
        String sql = String.format("Select * From wallet JOIN `order` ON `order`.buyer = wallet.IBAN" +
                "WHERE orderId = ? AND type = '%s';", TransactionType.TRANSACTION.toString());
        Wallet wallet = null;
        try {
            wallet = jdbcTemplate.queryForObject(sql, new walletRowMapper(), orderId);
        } catch (DataAccessException dataAccessException) {
            if (! dataAccessException.getMessage().toString().equals("FindBuyerWalletByOrderID: Incorrect result size: expected 1, actual 0")) {
                System.err.println(dataAccessException.getMessage());}
        }
        return wallet;
    }


    public void updateWalletAssets(Wallet wallet, Asset asset, double amount) {
        String sql = "Update wallet_has_asset Set amount = ? Where IBAN = ? And code = ?;";
        jdbcTemplate.update(sql, amount, wallet.getIban(), asset.getCode());
    }

    public void createWalletAsset(Wallet wallet, Asset asset, double amount) {
        String sql = "Insert into wallet_has_asset values(?,?,?);";
        jdbcTemplate.update(sql, asset.getCode(), amount, wallet.getIban());
    }

    @Override
    public Double findAmountOfAsset(String iban, String assetCode) {
        String sql = "SELECT * FROM wallet_has_asset WHERE IBAN = ? AND code = ?;";

        try {
            return jdbcTemplate.queryForObject(sql, new Wallet_has_assetRowMapper(), iban, assetCode);
        }  catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return null;
    }


    public Wallet findWalletByEmail(String email) {
        String slq = "Select * From wallet JOIN client ON client.IBAN = wallet.IBAN Where email = ?;";
        Wallet wallet = null;
        try {
            wallet = jdbcTemplate.queryForObject(slq, new walletRowMapper(), email);
        } catch (DataAccessException dataAccessException) {
            if (! dataAccessException.getMessage().toString().equals("findWalletByEmail: Incorrect result size: expected 1, actual 0")) {
                System.err.println(dataAccessException.getMessage());}
        }
        return wallet;
    }


    public Wallet findWalletByBankCode(String bankCode) {
        String slq = "Select * From wallet JOIN bank ON bank.IBAN = wallet.IBAN Where code = ?;";
        Wallet wallet = null;
        try {
            wallet = jdbcTemplate.queryForObject(slq, new walletRowMapper(), bankCode);
        } catch (DataAccessException dataAccessException) {
            if (! dataAccessException.getMessage().toString().equals("findWalletByBankCode: Incorrect result size: expected 1, actual 0")) {
                System.err.println(dataAccessException.getMessage());}
        }
        return wallet;
    }


    public class walletRowMapper implements RowMapper<Wallet> {

        @Override
        public Wallet mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return new Wallet( resultSet.getString("IBAN"), resultSet.getInt("balance"));
        }
    }

    public class Wallet_has_assetRowMapper implements RowMapper<Double> {

        @Override
        public Double mapRow(ResultSet resultSet, int rowNum) throws SQLException {
            return resultSet.getDouble("amount");
        }
    }

}

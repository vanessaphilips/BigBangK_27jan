package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.List;

/**
 * JdbcClientDAO created by Vanessa Philips.
 */

@Repository
public class JdbcClientDAO implements IClientDAO {

    JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcClientDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveClient(Client mpClient) {
        String sql = "Insert into Client values(?,?,?,?,?,?,?,?,?,?);";//10 ?s
        try {
            jdbcTemplate.update(sql,
                    mpClient.getEmail(),
                    mpClient.getFirstName(),
                    mpClient.getInsertion(),
                    mpClient.getLastName(),
                    mpClient.getDateOfBirth(),
                    mpClient.getBsn(),
                    mpClient.getPassWord(),
                    mpClient.getAddress().getPostalCode(),
                    mpClient.getAddress().getNumber(),
                    mpClient.getWallet().getIban());
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
    }

    @Override
    public Client findClientByEmail(String email) {
        String sql = "SELECT * FROM Client WHERE email = ?;";
        Client client = null;
        try {
            client = jdbcTemplate.queryForObject(sql, new ClientRowMapper(), email);
        } catch (DataAccessException dataAccessException) {
            if (! dataAccessException.getMessage().toString().equals("Incorrect result size: expected 1, actual 0")) {
                System.err.println(dataAccessException.getMessage());
            }
        }
        return client;
    }

    @Override
    public List<Client> findAllClients() {
        String sql = "SELECT * FROM Client;";
        try {
            return jdbcTemplate.query(sql, new ClientRowMapper());
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return null;
    }

    @Override
    public void updateClient(Client client) {
        String sql = "UPDATE Client Set email = ?, firstName = ?, insertion = ?, " +
                "lastName = ?, dateOfBirth = ?, bsn = ?, passWord = ?, " +
                "address = ? wallet = ?, WHERE email = ?;";
        try {
            jdbcTemplate.update(sql, client.getEmail(), client.getFirstName(), client.getInsertion(),
                    client.getLastName(), client.getDateOfBirth(), client.getBsn(), client.getPassWord(),
                    client.getAddress(), client.getWallet());
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
    }

    @Override
    public List<Client> findClientByLastName(String lastName) {
        String sql = "SELECT * FROM Client WHERE lastName = ?;";
        try {
            return jdbcTemplate.query(sql, new ClientRowMapper(), lastName);
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return null;
    }

    // Note: in RowMapper alleen de attributen uit Client (dus niet Address en Wallet)
    private class ClientRowMapper implements RowMapper<Client> {
        @Override
        public Client mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
            return new Client(
                    resultSet.getString("email"),
                    resultSet.getString("firstName"),
                    resultSet.getString("insertion"),
                    resultSet.getString("lastName"),
                    resultSet.getDate("dateOfBirth").toLocalDate(),
                    resultSet.getString("bsn"),
                    resultSet.getString("passWord"));
        }
    }
}
// Created by RayS
// Creation date 2-12-2021

package com.example.project_bigbangk.repository;
import com.example.project_bigbangk.model.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class JdbcAddressDAO implements IAddressDAO {

    JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcAddressDAO(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public void saveAddress(Address mpAddress) {
        String sql = "Insert Ignore into address values(?,?,?,?,?)";
        try {
            jdbcTemplate.update(sql, mpAddress.getPostalCode(), mpAddress.getStreet(), mpAddress.getNumber(),
                    mpAddress.getCity(), mpAddress.getCountry());
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
    }

    @Override
    public Address findAddressByEmail(String email) {
        String sql = "Select * From addresswithclientemail Where email = ?";
        Address address = null;
        try {
            address = jdbcTemplate.queryForObject(sql, new JdbcAddressDAO.AddressRowMapper(), email);
        } catch (DataAccessException dataAccessException){
            if (! dataAccessException.getMessage().toString().equals("Incorrect result size: expected 1, actual 0")) {
            System.err.println(dataAccessException.getMessage());}
        }
        return address;
    }

    @Override
    public List<Address> findAllAddresses() {
        String sql = "Select * From address";
        try {
            return jdbcTemplate.query(sql, new AddressRowMapper());
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return null;
    }

    @Override
    public List<Address> findAddressByPostalcode(String postalcode) {
        String sql = "Select * From address Where postalcode = ?";
        try {
            return jdbcTemplate.query(sql, new AddressRowMapper(), postalcode);
        } catch (DataAccessException dataAccessException) {
            System.err.println(dataAccessException.getMessage());
        }
        return null;
    }

    private class AddressRowMapper implements RowMapper<Address> {
        @Override
        public Address mapRow(ResultSet resultSet, int rowNumber) throws SQLException {
            return new Address(resultSet.getString("postalcode"), resultSet.getString("street"),
                    resultSet.getInt("number"), resultSet.getString("city"),
                    resultSet.getString("country"));
        }
    }

}
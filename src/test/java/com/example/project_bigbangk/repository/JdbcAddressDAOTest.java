package com.example.project_bigbangk.repository;

/**
 * author: RayS
 */

import com.example.project_bigbangk.model.Address;
import com.example.project_bigbangk.model.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
//@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)

class JdbcAddressDAOTest {

    private final JdbcAddressDAO addressDAOTest;
    private final Client mockClient1 = Mockito.mock(Client.class);
    private Address newAddress1;
    private Address newAddress2;

    @BeforeEach
    public void setupTest() {
        Mockito.when(mockClient1.getEmail()).thenReturn("jan@jansen.nl");
        newAddress1 = new Address("3066JB", "Dijkstraat", 1, "Urk", "NL" );
        newAddress2 = new Address("1200AA", "Boserf", 12, "Groet", "NL");

    }
    @Autowired
    public JdbcAddressDAOTest (JdbcAddressDAO addressDAOTest) {
        super();
        this.addressDAOTest = addressDAOTest;
    }

    @Test
    void saveAddress() {
        addressDAOTest.saveAddress(newAddress1);
        System.out.println(newAddress1);
    }

    @Test
    void findAddressByEmail() {

    }

    @Test
    void findAllAddresses() {
        List<Address> actual = addressDAOTest.findAllAddresses();
        assertTrue(actual.size() == 6);
        assertTrue (actual.stream().filter(c-> c.getCity().equals("Rotterdam")).toArray().length == 3);

    }

    @Test
    void findAddressByPostalcode() {
    }
}
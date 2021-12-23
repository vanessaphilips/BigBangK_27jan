package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Address;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ClientDAO test created by Vanessa Philips.
 */

@SpringBootTest
@ActiveProfiles("test")
class JdbcClientDAOTest {

    private final JdbcClientDAO clientDAOTest;
    private final Wallet mockWallet = Mockito.mock(Wallet.class);
    private final Address mockAddress = Mockito.mock(Address.class);
    private Client newClient1;
    private Client newClient2;
    private Client updateClient1;
    private Client updateClient2;

    @BeforeEach
    public void setupTest() {
        Mockito.when(mockWallet.getIban()).thenReturn("NL57ABNA0999999999");
        Mockito.when(mockAddress.getPostalCode()).thenReturn("3072 HG");
        Mockito.when(mockAddress.getNumber()).thenReturn(14);
        newClient1 = new Client("client1@test.nl", "Client1", null, "Test1", LocalDate.of(1999, 9, 9), "123456789", "clienttest1", null, null);
        newClient2 = new Client("client2@test.nl", "Client2", null, "Test2", LocalDate.of(1988, 8, 8), "123456789", "clienttest2", null, null);
        updateClient1 = new Client("client1U@test.nl", "Client1U", null, "Test1U", LocalDate.of(1977, 7, 7), "123456789", "clienttest1U", null, null);
        updateClient2 = new Client("client2U@test.nl", "Client2U", null, "Test2U", LocalDate.of(1966, 6, 6), "123456789", "clienttest2U", null, null);
    }

    @Autowired
    public JdbcClientDAOTest(JdbcClientDAO clientDAOTest) {
        super();
        this.clientDAOTest = clientDAOTest;
    }

    @Test
    void saveClient() {
        newClient1.setAddress(mockAddress);
        newClient1.setWallet(mockWallet);
        clientDAOTest.saveClient(newClient1);
        System.out.println(newClient1);

        newClient2.setAddress(mockAddress);
        newClient2.setWallet(mockWallet);
        clientDAOTest.saveClient(newClient2);
        System.out.println(newClient2);
    }

    @Test
    void findClientByEmail() {
        Client actual1 = clientDAOTest.findClientByEmail("sander@deboer.nl");
        Client expected1 = new Client("sander@deboer.nl", "Sander", "de",
                "Boer", LocalDate.of(1966,9,9), "123456789",
                "sanderdeboer", null, null);
        assertThat(expected1).isEqualTo(actual1);

        Client actual2 = clientDAOTest.findClientByEmail("nicole@wong.nl");
        Client expected2 = new Client("nicole@wong.nl", "Nicole", "",
                "Wong", LocalDate.of(1973, 01, 01), "123456789",
                "nicolewong", null, null);
        assertEquals(expected2, actual2);
    }

    @Test
    void findAllClients() {

        //FIXME!

//        List<Client> actual = clientDAOTest.findAllClients();
//        List<Client> expected = new ArrayList<>();
//        expected.add(newClient1);
//        expected.add(newClient2);
//        assertThat(actual).isEqualTo(expected);
//        assertThat(actual.size()).isEqualTo(expected.size());
    }

    @Test
    void updateClient() {
        updateClient1.setAddress(mockAddress);
        updateClient1.setWallet(mockWallet);
        clientDAOTest.updateClient(updateClient1);
        System.out.println(updateClient1);
    }

    @Test
    void findClientByLastName() {

        //FIXME!
//
//        Client actual = clientDAOTest.findClientByLastName("Test1");
//        Client expected = newClient1;
//        assertThat(actual).isEqualTo(expected);

        //FIXME

//        Client harry = new Client("harry@scary.nl", "Harry", null, "Scary", LocalDate.of(1955, 5, 5), "123456789", "harryscary", null, null);
//        clientDAOTest.saveClient(harry);
//        Client testHarry = clientDAOTest.findClientByLastName("Scary");
//        String lastName = "Scary";
//        assertEquals(lastName, testHarry.getLastName());
    }
}
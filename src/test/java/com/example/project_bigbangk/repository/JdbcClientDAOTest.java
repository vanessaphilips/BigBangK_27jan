package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Address;
import com.example.project_bigbangk.model.Client;
import com.example.project_bigbangk.model.Wallet;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ClientDAO test created by Vanessa Philips.
 */

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
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
    @Order(1)
    void findAllClients() {
        List<Client> actual = clientDAOTest.findAllClients();
        assertTrue(actual.size() == 6);
        assertTrue (actual.stream().filter(c-> c.getEmail().equals("michael@oosterhout.nl")).toArray().length == 1);

        actual.add(newClient1);
        actual.add(newClient2);
        assertTrue(actual.size() == 8);
        assertTrue (actual.stream().filter(c-> c.getEmail().equals("client1@test.nl")).toArray().length == 1);
        assertTrue (actual.stream().filter(c-> c.getLastName().equals("Test2")).toArray().length == 1);
        assertFalse (actual.stream().filter(c-> c.getLastName().equals("Hoi")).toArray().length == 1);
    }

    @Test
    @Order(2)
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
    @Order(3)
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
    @Order(4)
    void updateClient() {
        updateClient1.setAddress(mockAddress);
        updateClient1.setWallet(mockWallet);
        clientDAOTest.updateClient(updateClient1);
        System.out.println(updateClient1);
    }

    @Test
    @Order(5)
    void findClientByLastName() {
        List<Client> actual1 = clientDAOTest.findClientByLastName("Oey");
        assertTrue(actual1.size() == 1);

        clientDAOTest.findClientByLastName("Vanessa");
        assertNotEquals("Vanessa", null);

        clientDAOTest.findClientByLastName("Philips");
        assertNull(null);
    }
}
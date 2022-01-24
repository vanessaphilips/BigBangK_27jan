package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.service.OrderMatchingService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class JdbcWalletDAOTest {
    @MockBean
    OrderMatchingService orderMatchingService;
    private IWalletDAO walletDAOUnderTest;

    @Autowired
    public JdbcWalletDAOTest(IWalletDAO walletDAOUnderTest) {
        super();
        this.walletDAOUnderTest = walletDAOUnderTest;
    }

    @Test
    @Order(1)
    void findWalletByIban() {
        Wallet actual = walletDAOUnderTest.findWalletByIban("NL20BGBK0001234567");

        Wallet expected = new Wallet("NL20BGBK0001234567", 10000.00);

        assertThat(expected).isEqualTo(actual);
    }

    @Test
    @Order(2)
    void saveNewWallet() {
        Wallet savedWallet = new Wallet("NL30BGBK0007654321", 20000.00);
        walletDAOUnderTest.saveNewWallet(savedWallet);

        Wallet actual = walletDAOUnderTest.findWalletByIban("NL30BGBK0007654321");

        Wallet expected = new Wallet("NL30BGBK0007654321", 20000.00);
        assertThat(expected).isEqualTo(actual);
    }

    @Test
    @Order(3)
    void updateBalance() {
        Wallet actual = walletDAOUnderTest.findWalletByIban("NL20BGBK0001234567");
        actual.setBalance(12000.00);
        walletDAOUnderTest.updateBalance(actual);

        Wallet expected = new Wallet("NL20BGBK0001234567", 12000.00);

        actual = walletDAOUnderTest.findWalletByIban("NL20BGBK0001234567");
        assertThat(expected).isEqualTo(actual);
    }

//    //TODO test maken
//    @Test
//    @Order(4)
//    void updateWalletAssets() {
//        fail("Nog te maken test");
//
//    }
//
//    //TODO test maken
//    @Test
//    @Order(5)
//    void findAmountOfAsset() {
//        fail("Nog te maken test");
//
//    }
}
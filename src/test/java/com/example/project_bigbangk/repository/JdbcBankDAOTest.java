package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Bank;
import com.example.project_bigbangk.model.Wallet;
import com.example.project_bigbangk.service.OrderMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Hier wordt de JdbcBankDOA gestest.
 *
 * @Author Kelly Speelman - de Jonge
 */

@SpringBootTest
@ActiveProfiles("test")
class JdbcBankDAOTest {
    @MockBean
    OrderMatchingService orderMatchingService;

    private final JdbcBankDAO jdbcBankDAOTest;
    private final Wallet mockWallet = Mockito.mock(Wallet.class);
    private Bank newBank;
    private Bank bigBangk;
    private Bank newBankUpdate;

    @BeforeEach
    public void startUp (){
        Mockito.when(mockWallet.getIban()).thenReturn("NL17 BGBK 7265511");
        newBank = new Bank("Test bank", "TSBK", 3.0, 210.00);
        bigBangk = new Bank("Big Bangk", "BGBK", 5.0, 1000.00);
        newBankUpdate = new Bank("Test bank aangepast", "TSBK", 9.0, 6710.00);
    }

    @Autowired
    public JdbcBankDAOTest(JdbcBankDAO doaUnderTest) {
        super();
        this.jdbcBankDAOTest = doaUnderTest;
    }

    @Test
    void saveBank() {
        assertThat(jdbcBankDAOTest.findAllBank().size()).isEqualTo(1);
        newBank.setWallet(mockWallet);
        jdbcBankDAOTest.saveBank(newBank);

        assertThat(jdbcBankDAOTest.findAllBank().size()).isEqualTo(2);

        newBankUpdate.setWallet(mockWallet);
        jdbcBankDAOTest.saveBank(newBankUpdate);
        assertThat(jdbcBankDAOTest.findAllBank().size()).isEqualTo(2);
    }

    @Test
    void findBank() {
        Bank actual = jdbcBankDAOTest.findBank("Big Bangk");
        Bank expected = bigBangk;
        assertThat(actual).isEqualTo(expected);

        actual = jdbcBankDAOTest.findBank("Test bank");
        expected = newBank;
        assertThat(actual).isEqualTo(expected).isNotNull();

        actual = jdbcBankDAOTest.findBank("Geen bank");
        assertThat(actual).isNull();
    }

    @Test
    void updateBank() {
        assertThat(jdbcBankDAOTest.findBank(newBank.getName()).getCode()).isEqualTo(newBank.getCode());
        jdbcBankDAOTest.updateBank(newBankUpdate);
        assertThat(jdbcBankDAOTest.findBank(newBankUpdate.getName()).getCode()).isEqualTo(newBank.getCode());

        Bank bestaatNiet = new Bank("No bank", "NONO", 3.0, 2.0);
        jdbcBankDAOTest.updateBank(bestaatNiet);
        assertThat(jdbcBankDAOTest.findBank(bestaatNiet.getName())).isNull();
        assertThat(jdbcBankDAOTest.findAllBank().size()).isEqualTo(2);
    }

    @Test
    void findAllBank() {
        List<Bank> actual = jdbcBankDAOTest.findAllBank();
        List<Bank> expected = new ArrayList<>();
        expected.add(bigBangk);
        expected.add(newBankUpdate);
        assertThat(actual).isEqualTo(expected).size().isEqualTo(expected.size());
    }
}
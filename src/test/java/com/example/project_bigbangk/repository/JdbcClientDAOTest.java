package com.example.project_bigbangk.repository;

import com.example.project_bigbangk.model.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class JdbcClientDAOTest {

    private IClientDAO clientDAOUnderTest;

    @Autowired
    public JdbcClientDAOTest(IClientDAO clientDAOUnderTest) {
        super();
        this.clientDAOUnderTest = clientDAOUnderTest;
    }

    @Test
    public void setupTest() {
        assertThat(clientDAOUnderTest).isNotNull();
    }

    // TODO onderstaande nog in te vullen!

    @Test
    void saveClient() {
    }

    @Test
    void findClientByEmail() {
        System.out.println("test0 ");
        Client actual = clientDAOUnderTest.findClientByEmail("sander@deboer.nl");
        System.out.println("test1");
        Client expected = new Client("Sander", "de", "Boer", "sander@deboer.nl",
                "123456789", new Date(1966, 9,9), "sanderdeboer");
        System.out.println("test2");
        assertThat(expected).isEqualTo(actual);
    }

    @Test
    void findAllClients() {
        fail("Test not yet implemented");
    }

    @Test
    void updateClient() {
        fail("Test not yet implemented");
    }

    @Test
    void findClientByLastName() {
        fail("Test not yet implemented");
    }
}
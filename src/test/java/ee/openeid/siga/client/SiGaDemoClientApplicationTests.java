package ee.openeid.siga.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@TestPropertySource(properties = "siga.api.trust-store=classpath:empty.p12")
class SiGaDemoClientApplicationTests {

    @Test
    void contextLoads() {
        assertTrue(true);
    }

}

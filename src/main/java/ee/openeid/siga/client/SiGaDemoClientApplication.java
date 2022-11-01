package ee.openeid.siga.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SiGaDemoClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(SiGaDemoClientApplication.class, args);
    }
}

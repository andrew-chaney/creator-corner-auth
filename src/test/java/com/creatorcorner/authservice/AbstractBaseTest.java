package com.creatorcorner.authservice;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractBaseTest {

    protected WebTestClient client;

    @LocalServerPort
    int port = 0;

    @BeforeEach
    protected void setup() {
        this.client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:%d".formatted(port))
                .responseTimeout(Duration.ofMinutes(1L))
                .build();
    }
}

package com.demo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test — verifies the Spring context loads and the health endpoint responds.
 * Exercises the full stack: Spring Boot 2.6.3 + Tomcat 9.0.60 + Log4j 2.14.1.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("Application context loads")
    void contextLoads() {
        // If context fails to start, this test fails — no assertions needed
    }

    @Test
    @DisplayName("Health endpoint returns 200")
    void healthEndpointReturns200() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("http://localhost:" + port + "/api/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    @DisplayName("Products endpoint requires authentication")
    void productsRequiresAuth() {
        ResponseEntity<String> response = restTemplate
                .getForEntity("http://localhost:" + port + "/api/products", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @DisplayName("Authenticated products endpoint returns list")
    void authenticatedProductsEndpoint() {
        ResponseEntity<String> response = restTemplate
                .withBasicAuth("admin", "admin123")
                .getForEntity("http://localhost:" + port + "/api/products", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("Widget");
    }
}

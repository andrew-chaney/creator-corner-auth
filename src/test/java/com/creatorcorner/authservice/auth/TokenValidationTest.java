package com.creatorcorner.authservice.auth;

import com.creatorcorner.authservice.AbstractBaseTest;
import com.creatorcorner.authservice.authentication.BearerToken;
import com.creatorcorner.authservice.dto.LoginDto;
import com.creatorcorner.authservice.dto.UserDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.util.Date;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TokenValidationTest extends AbstractBaseTest {

    @Value("${jwt.secret}")
    private String secret;

    @Test
    @DisplayName("Can validate a logged-in user")
    void validatedWhenLoggedIn() {
        UserDto userRequest = UserDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("johndoe@john.doe")
                .password("Jjj_jj12j")
                .build();

        LoginDto loginRequest = LoginDto.builder()
                .email("johndoe@john.doe")
                .password("Jjj_jj12j")
                .build();

        // Register the user
        this.client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().isCreated();

        // Log the user in and save the bearer token
        HttpHeaders responseHeaders = this.client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.AUTHORIZATION)
                .returnResult(Void.class)
                .getResponseHeaders();

        // Test the bearer token
        String providedToken = responseHeaders.getFirst(HttpHeaders.AUTHORIZATION);
        assertThat(providedToken, is(notNullValue()));
        assertThat(providedToken.substring(0, 7), is(equalTo("Bearer ")));

        // Run the bearer token through the validation endpoint
        this.client.get().uri("/validate")
                .header(HttpHeaders.AUTHORIZATION, providedToken)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("An expired bearer token is not valid")
    void invalidWhenExpired() {
        // Create an expired token for the existing user
        Date expiration = new Date(System.currentTimeMillis() + (1000L * 5));

        BearerToken token = new BearerToken(
                Jwts.builder()
                        .setSubject("johndoe@john.doe")
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setExpiration(expiration) // Valid for 10 seconds
                        .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                        .compact()
        );

        // Validate the token while it is valid
        this.client.get().uri("/validate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getValue())
                .exchange()
                .expectStatus().isOk();

        // Using Awaitility to wait until the cookie is considered expired, to not have to wait more than necessary
        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> expiration.before(new Date(System.currentTimeMillis())));

        this.client.get().uri("/validate")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getValue())
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Only valid bearer tokens are accepted")
    void valueMustBeValid() {
        this.client.get().uri("/validate")
                .header(HttpHeaders.AUTHORIZATION, "not-a.valid-bearer.token")
                .exchange()
                .expectStatus().isBadRequest();
    }
}

package com.creatorcorner.authservice.user;

import com.creatorcorner.authservice.AbstractBaseTest;
import com.creatorcorner.authservice.authentication.AuthToken;
import com.creatorcorner.authservice.dto.LoginDto;
import com.creatorcorner.authservice.dto.UserDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.Duration;
import java.util.Date;
import java.util.Objects;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

class UserCookieValidationTest extends AbstractBaseTest {

    @Value("${jwt.cookie-name}")
    private String cookieName;

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

        MultiValueMap<String, String> testCookies = new LinkedMultiValueMap<>();

        // Register the user
        this.client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRequest)
                .exchange()
                .expectStatus().isCreated();

        // Log the user in and save the cookie
        MultiValueMap<String, ResponseCookie> cookies = this.client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists(cookieName)
                .returnResult(Void.class)
                .getResponseCookies();

        // Put the cookies into the testCookies for the validation request
        for (String key : cookies.keySet()) {
            testCookies.add(key, Objects.requireNonNull(cookies.getFirst(key)).getValue());
        }

        // Test the cookie
        ResponseCookie authCookie = cookies.getFirst(cookieName);
        assertThat(authCookie, is(notNullValue()));
        assertThat(authCookie.isHttpOnly(), is(Boolean.TRUE));

        // Run the cookie through the validation endpoint
        this.client.get().uri("/validate")
                .cookies(reqCookies -> reqCookies.addAll(testCookies))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("An expired cookie is not valid")
    void invalidWhenExpired() {
        // Create an expired token for the existing user
        Date expiration = new Date(System.currentTimeMillis() + (1000L * 5));

        ResponseCookie cookie = ResponseCookie.from(cookieName)
                .value(new AuthToken(
                                Jwts.builder()
                                        .setSubject("johndoe@john.doe")
                                        .setIssuedAt(new Date(System.currentTimeMillis()))
                                        .setExpiration(expiration) // Valid for 10 seconds
                                        .signWith(Keys.hmacShaKeyFor(secret.getBytes()))
                                        .compact()
                        ).getValue()
                )
                .build();

        MultiValueMap<String, String> customCookies = new LinkedMultiValueMap<>();
        customCookies.add(cookieName, cookie.getValue());

        // Validate the token while it is valid
        this.client.get().uri("/validate")
                .cookies(reqCookies -> reqCookies.addAll(customCookies))
                .exchange()
                .expectStatus().isOk();

        // Using Awaitility to wait until the cookie is considered expired, to not have to wait more than necessary
        await()
                .atMost(Duration.ofSeconds(10))
                .until(() -> expiration.before(new Date(System.currentTimeMillis())));

        this.client.get().uri("/validate")
                .cookies(reqCookies -> reqCookies.addAll(customCookies))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("Only valid cookie values are accepted")
    void valueMustBeValid() {
        this.client.get().uri("/validate")
                .cookie(cookieName, "not-a-valid-cookie-value")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}

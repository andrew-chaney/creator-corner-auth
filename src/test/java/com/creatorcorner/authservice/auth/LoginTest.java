package com.creatorcorner.authservice.auth;

import com.creatorcorner.authservice.AbstractBaseTest;
import com.creatorcorner.authservice.authentication.BearerToken;
import com.creatorcorner.authservice.authentication.JwtSupport;
import com.creatorcorner.authservice.dto.LoginDto;
import com.creatorcorner.authservice.dto.UserDto;
import com.creatorcorner.authservice.entity.User;
import com.creatorcorner.authservice.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class LoginTest extends AbstractBaseTest {

    @Autowired
    private JwtSupport jwtSupport;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Can login as a valid user")
    void canLoginValidUser() {
        UserDto testUser = UserDto.builder()
                .firstName("Johnny")
                .lastName("Appleseed")
                .email("johnny.appleseed@hotmail.com")
                .password("Its_Johnny!010")
                .build();

        LoginDto loginRequest = LoginDto.builder()
                .email(testUser.getEmail())
                .password(testUser.getPassword())
                .build();

        // Register the test user
        this.client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(testUser)
                .exchange()
                .expectStatus().isCreated();

        // Log the user in
        HttpHeaders result = this.client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().exists(HttpHeaders.AUTHORIZATION)
                .returnResult(Void.class)
                .getResponseHeaders();

        // Create the BearerToken from the provided token
        String providedToken = result.getFirst(HttpHeaders.AUTHORIZATION);
        assertThat(providedToken, is(notNullValue()));
        assertThat(providedToken.substring(0, 7), is(equalTo("Bearer ")));

        BearerToken resultingToken = new BearerToken(providedToken.substring(7));

        // Get the entity for the test user
        User testUserEntity = userRepository.findByEmail(testUser.getEmail()).block();

        // Test the contents of the bearer token
        assertThat(jwtSupport.getUserEmail(resultingToken), equalTo(testUser.getEmail()));
        assertThat(jwtSupport.isValidToken(resultingToken, testUserEntity), is(Boolean.TRUE));
    }

    @Test
    @DisplayName("LoginDto must include an email")
    void rejectWithoutEmail() {
        LoginDto loginRequest = LoginDto.builder()
                .password("Its_Johnny!010")
                .build();

        this.client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("LoginDto must include a password")
    void rejectWithoutPassword() {
        LoginDto loginRequest = LoginDto.builder()
                .email("johnny.appleseed@hotmail.com")
                .build();

        this.client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Non-existent emails are rejected")
    void rejectInvalidEmail() {
        LoginDto loginRequest = LoginDto.builder()
                .email("not_a_user@gmail.com")
                .password("Its_Johnny!010")
                .build();

        this.client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Invalid password for a user is rejected")
    void rejectBadPassword() {
        LoginDto loginRequest = LoginDto.builder()
                .email("johnny.appleseed@hotmail.com")
                .password("not_Johnnys-Password32")
                .build();

        this.client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Reject empty body")
    void rejectEmptyBody() {
        this.client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("")
                .exchange()
                .expectStatus().isBadRequest();
    }
}

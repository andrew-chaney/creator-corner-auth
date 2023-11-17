package com.creatorcorner.authservice.user;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class UserLoginTest extends AbstractBaseTest {

    @Autowired
    private JwtSupport jwtSupport;

    @Autowired
    private UserRepository userRepository;

    @Value("${jwt.cookie-name}")
    private String cookieName;

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
        MultiValueMap<String, ResponseCookie> result = this.client.post().uri("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectCookie().exists(cookieName)
                .returnResult(Void.class)
                .getResponseCookies();

        // Create the BearerToken from the provided cookie
        ResponseCookie authCookie = result.getFirst(cookieName);
        assertThat(authCookie, is(notNullValue()));

        BearerToken resultingToken = new BearerToken(authCookie.getValue());

        // Get the entity for the test user
        User testUserEntity = userRepository.findByEmail(testUser.getEmail()).block();

        // Test the contents of the cookie
        assertThat(authCookie.isHttpOnly(), is(Boolean.TRUE));
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

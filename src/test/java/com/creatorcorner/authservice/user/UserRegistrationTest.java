package com.creatorcorner.authservice.user;

import com.creatorcorner.authservice.AbstractBaseTest;
import com.creatorcorner.authservice.dto.UserDto;
import com.creatorcorner.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class UserRegistrationTest extends AbstractBaseTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void refreshDB() {
        userRepository.deleteAll().block();
    }

    @Test
    @DisplayName("Can register a valid user, and get a valid response object.")
    void canRegisterValidUser() {
        UserDto request = UserDto.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@email.com")
                .password("Super_Secure_Password123!")
                .build();

        this.client.post().uri("/register")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserDto.class)
                .consumeWith(exchangeResult -> {
                    UserDto response = exchangeResult.getResponseBody();
                    testSuccessfulUserResponse(response, request);
                });
    }

    @Test
    @DisplayName("Cannot register two users with the same email")
    void cannotDuplicateEmails() {
        UserDto request = UserDto.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@email.com")
                .password("Super_Secure_Password123!")
                .build();

        // Can register the first object.
        this.client.post().uri("/register")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserDto.class)
                .consumeWith(exchangeResult -> {
                    UserDto response = exchangeResult.getResponseBody();
                    testSuccessfulUserResponse(response, request);
                });

        // Cannot register the second object
        this.client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Cannot register a user without a first name")
    void cannotRegisterWithoutFirstName() {
        UserDto request = UserDto.builder()
                .lastName("Smith")
                .email("john.smith@email.com")
                .password("Super_Secure_Password123!")
                .build();

        this.client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Cannot register a user without a last name")
    void cannotRegisterWithoutLastName() {
        UserDto request = UserDto.builder()
                .firstName("John")
                .email("john.smith@email.com")
                .password("Super_Secure_Password123!")
                .build();

        this.client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }


    @Test
    @DisplayName("Cannot register a user without an email")
    void cannotRegisterWithoutEmail() {
        UserDto request = UserDto.builder()
                .firstName("John")
                .lastName("Smith")
                .password("Super_Secure_Password123!")
                .build();

        this.client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("Cannot register a user without a password")
    void cannotRegisterWithoutPassword() {
        UserDto request = UserDto.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@email.com")
                .build();

        this.client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",                 // Empty string
            "Sh0rt!",           // Too short
            "white Space123!",  // Presence of white-space
            "N12345678!",       // No lowercase letter
            "no_upper123!",     // No uppercase letter
            "no_Number_Here",   // No number
            "noSpecial1234",    // No special character
    })
    @DisplayName("Cannot register a user with an invalid password")
    void cannotRegisterWithInvalidPassword(String testString) {
        UserDto request = UserDto.builder()
                .firstName("John")
                .lastName("Smith")
                .email("john.smith@email.com")
                .password(testString)
                .build();

        this.client.post().uri("/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest();
    }

    private void testSuccessfulUserResponse(UserDto response, UserDto request) {
        assertThat(response, is(notNullValue()));
        assertThat(response.getUserId(), is(notNullValue()));
        assertThat(response.getFirstName(), equalTo(request.getFirstName()));
        assertThat(response.getLastName(), equalTo(request.getLastName()));
        assertThat(response.getEmail(), equalTo(request.getEmail()));
        assertThat(response.getCreatedTsEpoch(), is(notNullValue()));
        assertThat(response.getUpdatedTsEpoch(), is(notNullValue()));
    }
}

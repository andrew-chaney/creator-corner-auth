package com.creatorcorner.authservice.user;

import com.creatorcorner.authservice.AbstractBaseFT;
import com.creatorcorner.authservice.dto.UserDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

class GetUserFT extends AbstractBaseFT {

    @Test
    @DisplayName("Can get an existing user")
    void canGetExistingUser() {
        UserDto request = UserDto.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email("jane.smith@email.com")
                .password("Super_Secure_Password123!")
                .build();

        // Create the user
        EntityExchangeResult<UserDto> createResult = this.client.post().uri("/user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserDto.class)
                .returnResult();

        UserDto createResponse = createResult.getResponseBody();
        testSuccessfulUserResponse(createResponse, request);

        // Get the user by its ID
        this.client.get().uri("/user/{id}", createResponse.getUserId())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserDto.class)
                .consumeWith(exchangeResult -> {
                    UserDto getResponse = exchangeResult.getResponseBody();
                    assertThat(getResponse.getUserId(), equalTo(createResponse.getUserId()));
                    assertThat(getResponse.getFirstName(), equalTo(createResponse.getFirstName()));
                    assertThat(getResponse.getLastName(), equalTo(createResponse.getLastName()));
                    assertThat(getResponse.getEmail(), equalTo(createResponse.getEmail()));
                    assertThat(
                            getResponse.getCreatedTsEpoch().withNano(0),
                            equalTo(createResponse.getCreatedTsEpoch().withNano(0))
                    );
                    assertThat(
                            getResponse.getUpdatedTsEpoch().withNano(0),
                            equalTo(createResponse.getUpdatedTsEpoch().withNano(0))
                    );
                });
    }

    @Test
    @DisplayName("Cannot get a user that doesn't exist")
    void cannotGetNonexistentUser() {
        this.client.get().uri("/user/{id}", UUID.randomUUID())
                .exchange()
                .expectStatus().isNotFound();
    }

    // Cannot make a request with an ID that isn't a UUID
    @Test
    @DisplayName("Cannot make a request with an ID that isn't a UUID")
    void mustBeUUID() {
        this.client.get().uri("/user/{id}", "not-an-id")
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

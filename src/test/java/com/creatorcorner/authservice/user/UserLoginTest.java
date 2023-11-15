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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.util.MultiValueMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class UserLoginTest extends AbstractBaseTest {

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
                .password("Its_Jonny!010")
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
                .expectCookie().exists("creator_corner")
                .returnResult(String.class)
                .getResponseCookies();

        // Create the BearerToken from the provided cookie
        BearerToken resultingToken = new BearerToken(result.getFirst("creator_corner").getValue());

        // Get the entity for the test user
        User testUserEntity = userRepository.findByEmail(testUser.getEmail()).block();

        // Test the contents of the JWT
        assertThat(jwtSupport.getUserEmail(resultingToken), equalTo(testUser.getEmail()));
        assertThat(jwtSupport.isValidToken(resultingToken, testUserEntity), is(Boolean.TRUE));
    }
}

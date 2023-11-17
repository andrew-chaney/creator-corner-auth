package com.creatorcorner.authservice.service;

import com.creatorcorner.authservice.authentication.AuthToken;
import com.creatorcorner.authservice.authentication.JwtSupport;
import com.creatorcorner.authservice.dto.LoginDto;
import com.creatorcorner.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthService {

    private final JwtSupport jwtSupport;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    @Value("${jwt.duration-hours}")
    private int durationHours;
    @Value("${jwt.cookie-name}")
    private String cookieName;

    public Mono<ResponseCookie> login(LoginDto loginDto) {
        return userRepository.findByEmail(loginDto.getEmail())
                .flatMap(user -> {
                    if (passwordEncoder.matches(loginDto.getPassword(), user.getHashedPassword())) {
                        log.info("Serving token to user: {}", user.getEmail());
                        return Mono.just(user.getEmail());
                    }
                    log.info("Invalid password for user '{}', not serving token", user.getEmail());
                    return Mono.empty();
                })
                .map(jwtSupport::generateToken)
                .map(this::buildCookieFromToken);
    }

    public Mono<Boolean> validate(HttpCookie httpCookie) {
        AuthToken token = new AuthToken(httpCookie.getValue());
        return userRepository.findByEmail(jwtSupport.getUserEmail(token))
                .flatMap(user -> jwtSupport.isValidToken(token, user) ? Mono.just(Boolean.TRUE) : Mono.empty());
    }

    private ResponseCookie buildCookieFromToken(AuthToken authToken) {
        return ResponseCookie.from(cookieName).build()
                .mutate()
                .value(authToken.getValue())
                .httpOnly(true)
                .maxAge(Duration.ofHours(durationHours))
                .build();
    }
}

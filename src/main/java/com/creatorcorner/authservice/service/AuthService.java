package com.creatorcorner.authservice.service;

import com.creatorcorner.authservice.authentication.BearerToken;
import com.creatorcorner.authservice.authentication.JwtSupport;
import com.creatorcorner.authservice.dto.LoginDto;
import com.creatorcorner.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Service
public class AuthService {

    private final JwtSupport jwtSupport;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public Mono<BearerToken> login(LoginDto loginDto) {
        return userRepository.findByEmail(loginDto.getEmail())
                .flatMap(user -> {
                    if (passwordEncoder.matches(loginDto.getPassword(), user.getHashedPassword())) {
                        log.info("Serving token to user: {}", user.getEmail());
                        return Mono.just(user.getEmail());
                    }
                    log.info("Invalid password for user '{}', not serving token", user.getEmail());
                    return Mono.empty();
                })
                .map(jwtSupport::generateToken);
    }

    public Mono<Boolean> validate(String tokenValue) {
        BearerToken token;
        if (tokenValue.startsWith("Bearer ")) {
            token = new BearerToken(tokenValue.substring(7));
        } else {
            token = new BearerToken(tokenValue);
        }

        try {
            return userRepository.findByEmail(jwtSupport.getUserEmail(token))
                    .flatMap(user -> jwtSupport.isValidToken(token, user) ? Mono.just(Boolean.TRUE) : Mono.empty());
        } catch (Exception exception) {
            log.info("Invalid token received: {}", tokenValue);
            return Mono.empty();
        }
    }
}

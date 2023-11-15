package com.creatorcorner.authservice.service;

import com.creatorcorner.authservice.authentication.BearerToken;
import com.creatorcorner.authservice.authentication.JwtSupport;
import com.creatorcorner.authservice.dto.LoginDto;
import com.creatorcorner.authservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
@Service
public class AuthService {

    private JwtSupport jwtSupport;
    private PasswordEncoder passwordEncoder;
    private UserRepository userRepository;

    public Mono<BearerToken> login(LoginDto loginDto) {
        return userRepository.findByEmail(loginDto.getEmail())
                .flatMap(user -> {
                    if (passwordEncoder.matches(loginDto.getPassword(), user.getHashedPassword())) {
                        log.info("Serving token to user: {}", user.getEmail());
                        return Mono.just(jwtSupport.generateToken(user.getEmail()));
                    }
                    return Mono.empty();
                });
    }
}

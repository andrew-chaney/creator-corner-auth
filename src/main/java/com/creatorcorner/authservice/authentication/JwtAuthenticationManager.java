package com.creatorcorner.authservice.authentication;

import com.creatorcorner.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtSupport jwtSupport;
    private final UserRepository userRepository;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .filter(auth -> auth.getClass() == AuthToken.class)
                .cast(AuthToken.class)
                .flatMap(this::validate)
                .onErrorMap(error -> new InvalidAuthToken(error.getMessage()));
    }

    private Mono<Authentication> validate(AuthToken authToken) {
        return userRepository.findByEmail(jwtSupport.getUserEmail(authToken))
                .flatMap(user -> {
                    if (jwtSupport.isValidToken(authToken, user)) {
                        return Mono.just((Authentication) new UsernamePasswordAuthenticationToken(user.getEmail(), user.getHashedPassword()));
                    }
                    return Mono.error(new ServerWebInputException("Invalid authentication cookie provided"));
                });
    }
}

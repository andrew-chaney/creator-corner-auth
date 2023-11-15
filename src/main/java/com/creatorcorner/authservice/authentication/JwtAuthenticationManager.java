package com.creatorcorner.authservice.authentication;

import com.creatorcorner.authservice.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private JwtSupport jwtSupport;
    private UserRepository userRepository;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        return Mono.justOrEmpty(authentication)
                .filter(auth -> auth.getClass() == BearerToken.class)
                .cast(BearerToken.class)
                .map(this::validate)
                .onErrorMap(error -> new InvalidBearerToken(error.getMessage()));
    }

    private Authentication validate(BearerToken bearerToken) {
        return userRepository.findByEmail(jwtSupport.getUserEmail(bearerToken))
                .<UsernamePasswordAuthenticationToken>handle((user, sink) -> {
                    if (jwtSupport.isValidToken(bearerToken, user)) {
                        sink.next(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getHashedPassword()));
                        return;
                    }
                    sink.error(new IllegalArgumentException("Token is not valid"));
                })
                .block(); // TODO: fix this blocking call
    }
}

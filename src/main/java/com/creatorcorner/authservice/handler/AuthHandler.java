package com.creatorcorner.authservice.handler;

import com.creatorcorner.authservice.dto.LoginDto;
import com.creatorcorner.authservice.service.AuthService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@AllArgsConstructor
@Slf4j
@Component
public class AuthHandler {

    private AuthService authService;
    private Validator validator;

    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoginDto.class)
                .doOnNext(this::validate)
                .doOnNext(login -> log.info("Processing login attempt for user: {}", login.getEmail()))
                .flatMap(authService::login)
                .doOnNext(token -> log.info("Token: {}", token.getValue()))
                .flatMap(token -> ServerResponse.ok()
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getValue())
                        .build()
                )
                .switchIfEmpty(ServerResponse.badRequest().bodyValue("Invalid credentials provided"));
    }

    private void validate(LoginDto loginDto) {
        Errors errors = new BeanPropertyBindingResult(loginDto, "loginDto");
        this.validator.validate(loginDto, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
}
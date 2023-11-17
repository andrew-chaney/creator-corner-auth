package com.creatorcorner.authservice.handler;

import com.creatorcorner.authservice.dto.LoginDto;
import com.creatorcorner.authservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Component
public class AuthHandler {

    private final AuthService authService;
    private final Validator validator;

    @Value("${jwt.cookie-name}")
    private String cookieName;

    public Mono<ServerResponse> login(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(LoginDto.class)
                .doOnNext(this::validate)
                .doOnNext(login -> log.info("Processing login attempt for user: {}", login.getEmail()))
                .flatMap(authService::login)
                .flatMap(authCookie -> ServerResponse.ok()
                        .cookie(authCookie)
                        .build()
                )
                .switchIfEmpty(ServerResponse.badRequest().bodyValue("Invalid credentials provided"));
    }

    public Mono<ServerResponse> validateCookie(ServerRequest serverRequest) {
        return Mono.justOrEmpty(serverRequest.cookies().getFirst(cookieName))
                .flatMap(authService::validate)
                .flatMap(valid -> ServerResponse.ok().build())
                .switchIfEmpty(ServerResponse.badRequest().bodyValue("Invalid cookie"));
    }

    private void validate(LoginDto loginDto) {
        Errors errors = new BeanPropertyBindingResult(loginDto, "loginDto");
        this.validator.validate(loginDto, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
}

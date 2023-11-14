package com.creatorcorner.authservice.handler;

import com.creatorcorner.authservice.dto.UserDto;
import com.creatorcorner.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Component
public class UserHandler {

    private final UserService userService;
    private final Validator validator;

    public Mono<ServerResponse> create(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UserDto.class)
                .doOnNext(this::validate)
                .doOnNext(user -> log.info("Handling creation request for: {}", user.creationLogString()))
                .flatMap(userService::createUser)
                .flatMap(userDto ->
                        ServerResponse.created(URI.create("/user/" + userDto.getUserId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(userDto)
                )
                .switchIfEmpty(ServerResponse.badRequest().build());

    }

    public Mono<ServerResponse> getById(ServerRequest serverRequest) {
        return Mono.just(serverRequest.pathVariable("id"))
                .doOnNext(id -> log.info("Handling get request for ID: {}", id))
                .<UUID>handle((id, sink) -> {
                    try {
                        sink.next(UUID.fromString(id));
                    } catch (IllegalArgumentException e) {
                        sink.error(new ServerWebInputException(String.format(
                                "User ID must be of the type UUID for ID: %s", id
                        )));
                        log.info("Invalid get request for ID ({}), not of type UUID", id);
                    }
                })
                .flatMap(userService::getUser)
                .flatMap(userDto -> ServerResponse.ok().bodyValue(userDto))
                .switchIfEmpty(ServerResponse.notFound().build())
                .map(response -> {
                    if (response.statusCode() == HttpStatus.OK) {
                        log.info("Returning user: {}", serverRequest.pathVariable("id"));
                    } else {
                        log.info("User does not exist with ID: {}", serverRequest.pathVariable("id"));
                    }
                    return response;
                });
    }

    private void validate(UserDto userDto) {
        Errors errors = new BeanPropertyBindingResult(userDto, "userDto");
        this.validator.validate(userDto, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
}

package com.creatorcorner.authservice.handler;

import com.creatorcorner.authservice.dto.UserDto;
import com.creatorcorner.authservice.service.UserService;
import lombok.RequiredArgsConstructor;
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
@Component
public class UserHandler {

    private final UserService userService;
    private final Validator validator;

    public Mono<ServerResponse> create(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(UserDto.class)
                .doOnNext(this::validate)
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
                .<UUID>handle((id, sink) -> {
                    try {
                        sink.next(UUID.fromString(id));
                    } catch (IllegalArgumentException e) {
                        sink.error(new ServerWebInputException("User ID must be of the type UUID"));
                    }
                })
                .flatMap(userService::getUser)
                .flatMap(userDto -> ServerResponse.ok().bodyValue(userDto))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    private void validate(UserDto userDto) {
        Errors errors = new BeanPropertyBindingResult(userDto, "userDto");
        this.validator.validate(userDto, errors);
        if (errors.hasErrors()) {
            throw new ServerWebInputException(errors.toString());
        }
    }
}

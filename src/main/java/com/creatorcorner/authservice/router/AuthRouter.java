package com.creatorcorner.authservice.router;

import com.creatorcorner.authservice.handler.AuthHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthRouter {

    @Bean
    public RouterFunction<ServerResponse> authRoutes(AuthHandler authHandler) {
        return route()
                .POST("/login", accept(MediaType.APPLICATION_JSON), authHandler::login)
                .build();
    }
}

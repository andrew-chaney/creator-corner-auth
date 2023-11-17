package com.creatorcorner.authservice.authentication;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Component
public class JwtAuthenticationConverter implements ServerAuthenticationConverter {

    @Value("${jwt.cookie-name}")
    private String cookieName;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return Mono.justOrEmpty(exchange.getRequest().getCookies().getFirst(cookieName))
                .map(HttpCookie::getValue)
                .map(BearerToken::new);
    }
}

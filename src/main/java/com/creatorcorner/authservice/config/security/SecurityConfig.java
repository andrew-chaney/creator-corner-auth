package com.creatorcorner.authservice.config.security;

import com.creatorcorner.authservice.authentication.JwtAuthenticationConverter;
import com.creatorcorner.authservice.authentication.JwtAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain filterChain(
            ServerHttpSecurity http,
            JwtAuthenticationConverter converter,
            JwtAuthenticationManager authManager
    ) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authManager);
        filter.setServerAuthenticationConverter(converter);

        http
                // All permitted
                .authorizeExchange(exchange -> exchange.pathMatchers(HttpMethod.GET, "/actuator/health").permitAll())
                .authorizeExchange(exchange -> exchange.pathMatchers(HttpMethod.POST, "/login").permitAll())
                .authorizeExchange(exchange -> exchange.pathMatchers(HttpMethod.POST, "/register").permitAll())
                .authorizeExchange(exchange -> exchange.pathMatchers("/validate").permitAll())
                // Authenticated permitted
                .authorizeExchange(exchange -> exchange.pathMatchers("/user/*").authenticated())
                // Filter
                .addFilterAt(filter, SecurityWebFiltersOrder.AUTHENTICATION)
                // Other
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}


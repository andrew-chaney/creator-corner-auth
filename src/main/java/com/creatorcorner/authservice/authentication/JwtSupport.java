package com.creatorcorner.authservice.authentication;

import com.creatorcorner.authservice.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.Objects;

@RequiredArgsConstructor
@Component
public class JwtSupport {

    @Value("${jwt.duration-hours}")
    private int durationHours;

    @Value("${jwt.secret}")
    private String secret;

    private JwtParser parser;

    public AuthToken generateToken(String userEmail) {
        return new AuthToken(
                Jwts.builder()
                        .setSubject(userEmail)
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setExpiration(new Date(System.currentTimeMillis() + (1000L * 3600L * durationHours)))
                        .signWith(getSigningKey())
                        .compact()
        );
    }

    public String getUserEmail(AuthToken authToken) {
        return getParser()
                .parseClaimsJws(authToken.getValue())
                .getBody()
                .getSubject();
    }

    public boolean isValidToken(AuthToken authToken, User user) {
        Claims claims = getParser().parseClaimsJws(authToken.getValue()).getBody();
        boolean unexpired = claims.getExpiration().after(new Date(System.currentTimeMillis()));

        return unexpired && (Objects.equals(claims.getSubject(), user.getEmail()));
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    private JwtParser getParser() {
        if (parser == null) {
            parser = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build();
        }
        return parser;
    }
}

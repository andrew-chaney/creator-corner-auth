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

    @Value("${jwt.duration}")
    private int durationMinutes;

    @Value("${jwt.secret}")
    private String secret;

    private JwtParser parser;

    public BearerToken generateToken(String userEmail) {
        return new BearerToken(
                Jwts.builder()
                        .setSubject(userEmail)
                        .setIssuedAt(new Date(System.currentTimeMillis()))
                        .setExpiration(new Date(System.currentTimeMillis() + (1000L * 60 * durationMinutes)))
                        .signWith(getSigningKey())
                        .compact()
        );
    }

    public String getUserEmail(BearerToken bearerToken) {
        return getParser()
                .parseClaimsJws(bearerToken.getValue())
                .getBody()
                .getSubject();
    }

    public boolean isValidToken(BearerToken bearerToken, User user) {
        Claims claims = getParser().parseClaimsJws(bearerToken.getValue()).getBody();
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

package com.creatorcorner.authservice.authentication;

import org.springframework.security.core.AuthenticationException;

public class InvalidBearerToken extends AuthenticationException {

    public InvalidBearerToken(String msg, Throwable cause) {

        super(msg, cause);
    }

    public InvalidBearerToken(String msg) {
        super(msg);
    }
}

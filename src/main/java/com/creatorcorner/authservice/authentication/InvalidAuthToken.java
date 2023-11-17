package com.creatorcorner.authservice.authentication;

import org.springframework.security.core.AuthenticationException;

public class InvalidAuthToken extends AuthenticationException {

    public InvalidAuthToken(String msg, Throwable cause) {
        super(msg, cause);
    }

    public InvalidAuthToken(String msg) {
        super(msg);
    }
}

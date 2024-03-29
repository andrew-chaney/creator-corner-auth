package com.creatorcorner.authservice.authentication;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

@Getter
@Setter
public class BearerToken extends AbstractAuthenticationToken {

    private String value;

    public BearerToken(String value) {
        super(AuthorityUtils.NO_AUTHORITIES);
        this.value = value;
    }

    @Override
    public Object getCredentials() {
        return value;
    }

    @Override
    public Object getPrincipal() {
        return value;
    }
}

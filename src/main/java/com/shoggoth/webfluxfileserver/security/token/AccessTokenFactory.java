package com.shoggoth.webfluxfileserver.security.token;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
@Component
public class AccessTokenFactory implements TokenFactory<Token, Authentication> {

    @Value("${spring.security.token.ttl-in-minutes}")
    private Long tokenTtlInMinutes;

    @Override
    public Token generate(Authentication authentication) {
        return new Token(
                authentication.getName(),
                new ArrayList<>(authentication.getAuthorities()),
                Instant.now(),
                Instant.now().plus(tokenTtlInMinutes, ChronoUnit.MINUTES)
        );
    }
}

package com.shoggoth.webfluxfileserver.security;

import com.shoggoth.webfluxfileserver.security.token.Token;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
@RequiredArgsConstructor
public class TokenPrincipal implements Principal {
    private final Token token;
    @Override
    public String getName() {
        return token.subject();
    }
}

package com.shoggoth.webfluxfileserver.security.token;

import com.shoggoth.webfluxfileserver.security.token.Token;

public interface TokenConverter {
    String convert(Token token);
    Token convert(String token);

}

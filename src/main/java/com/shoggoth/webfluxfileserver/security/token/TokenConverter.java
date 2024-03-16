package com.shoggoth.webfluxfileserver.security.token;


public interface TokenConverter {
    String convert(Token token);
    Token convert(String token);

}

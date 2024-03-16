package com.shoggoth.webfluxfileserver.security.token;

public interface TokenFactory<T, A> {
    T generate(A authentication);
}

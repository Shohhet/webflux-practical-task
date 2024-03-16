package com.shoggoth.webfluxfileserver.exception;

public class AuthenticationException extends ApiException {
    public AuthenticationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}

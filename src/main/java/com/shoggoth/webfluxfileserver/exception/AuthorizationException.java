package com.shoggoth.webfluxfileserver.exception;

public class AuthorizationException extends ApiException {
    public AuthorizationException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}

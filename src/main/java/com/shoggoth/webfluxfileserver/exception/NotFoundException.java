package com.shoggoth.webfluxfileserver.exception;

public class NotFoundException extends ApiException {
    public NotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}

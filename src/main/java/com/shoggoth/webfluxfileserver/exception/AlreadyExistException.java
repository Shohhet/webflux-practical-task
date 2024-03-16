package com.shoggoth.webfluxfileserver.exception;

public class AlreadyExistException extends ApiException{

    public AlreadyExistException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}

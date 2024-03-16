package com.shoggoth.webfluxfileserver.exception;

import lombok.Getter;

@Getter
public class ApiException extends RuntimeException{
    ErrorCode errorCode;
    
    public ApiException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}

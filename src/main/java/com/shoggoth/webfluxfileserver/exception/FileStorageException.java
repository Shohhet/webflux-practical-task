package com.shoggoth.webfluxfileserver.exception;

public class FileStorageException extends ApiException {
    public FileStorageException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }
}

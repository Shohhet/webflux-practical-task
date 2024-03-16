package com.shoggoth.webfluxfileserver.errorhandler;

import com.shoggoth.webfluxfileserver.exception.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.security.SignatureException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class FileServerErrorAttributes extends DefaultErrorAttributes {

    public FileServerErrorAttributes() {
        super();
    }

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        var errorAttributes = super.getErrorAttributes(request, options);
        var error = getError(request);
        var errorList = new ArrayList<Map<String, Object>>();
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        if (error instanceof AuthenticationException ||
            error instanceof ExpiredJwtException ||
            error instanceof SignatureException ||
            error instanceof MalformedJwtException) {

            httpStatus = HttpStatus.UNAUTHORIZED;
            errorList.add(setUpErrorMap(error));

        } else if (error instanceof NotFoundException) {
            httpStatus = HttpStatus.NOT_FOUND;
            errorList.add(setUpErrorMap(error));

        } else if (error instanceof AlreadyExistException) {
            httpStatus = HttpStatus.BAD_REQUEST;
            errorList.add(setUpErrorMap(error));

        } else if (error instanceof AuthorizationException) {
            httpStatus = HttpStatus.FORBIDDEN;
            errorList.add(setUpErrorMap(error));

        } else if (error instanceof FileStorageException) {
            errorList.add(setUpErrorMap(error));
        } else {
            var message = error.getMessage();
            if (message == null)
                message = error.getClass().getName();

            var errorMap = new LinkedHashMap<String, Object>();
            errorMap.put("code", "INTERNAL_ERROR");
            errorMap.put("message", message);
            errorList.add(errorMap);

        }

        var errors = new HashMap<String, Object>();
        errors.put("errors", errorList);
        errorAttributes.put("status", httpStatus.value());
        errorAttributes.put("errors", errors);
        return errorAttributes;
    }

    private Map<String, Object> setUpErrorMap(Throwable error) {
        var errorMap = new LinkedHashMap<String, Object>();
        errorMap.put("code", ((ApiException) error).getErrorCode());
        errorMap.put("message", error.getMessage());
        return errorMap;
    }
}

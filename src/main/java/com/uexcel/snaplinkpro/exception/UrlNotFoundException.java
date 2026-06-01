package com.uexcel.snaplinkpro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UrlNotFoundException extends BaseException {

    public UrlNotFoundException(String message, HttpStatus status) {
        super(message, status);
    }
}

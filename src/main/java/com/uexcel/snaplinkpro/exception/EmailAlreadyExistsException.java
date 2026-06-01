package com.uexcel.snaplinkpro.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class EmailAlreadyExistsException extends BaseException {
    public EmailAlreadyExistsException(String message, HttpStatus status) {
        super(message, status);
    }
}

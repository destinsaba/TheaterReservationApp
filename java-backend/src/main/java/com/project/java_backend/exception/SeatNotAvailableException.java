package com.project.java_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class SeatNotAvailableException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SeatNotAvailableException(String message) {
        super(message);
    }
}

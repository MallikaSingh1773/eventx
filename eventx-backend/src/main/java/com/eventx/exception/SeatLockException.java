package com.eventx.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class SeatLockException extends RuntimeException {
    public SeatLockException(String message) {
        super(message);
    }

    public SeatLockException(String message, Throwable cause) {
        super(message, cause);
    }
}

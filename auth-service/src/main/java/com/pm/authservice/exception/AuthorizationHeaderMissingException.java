package com.pm.authservice.exception;

public class AuthorizationHeaderMissingException extends RuntimeException {
    public AuthorizationHeaderMissingException(String message) {
        super(message);
    }
}

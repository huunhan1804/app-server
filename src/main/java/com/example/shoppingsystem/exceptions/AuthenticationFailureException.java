package com.example.shoppingsystem.exceptions;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationFailureException extends AuthenticationException {
    public AuthenticationFailureException(String message) {
        super(message);
    }
}

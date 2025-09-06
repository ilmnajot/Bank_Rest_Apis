package com.example.bankcards.exception;

public class CardExpiredException extends RuntimeException {
    public CardExpiredException(String message) {
        super(message);
    }
    
    public CardExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
package com.savchenko.bankapp.exception;

public class InsufficientFundsException extends RuntimeException {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    public InsufficientFundsException() {
        super("Insufficient funds");
    }
}

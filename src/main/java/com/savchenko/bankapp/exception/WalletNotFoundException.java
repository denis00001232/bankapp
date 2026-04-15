package com.savchenko.bankapp.exception;

public class WalletNotFoundException extends RuntimeException {
    
    public WalletNotFoundException(String message) {
        super(message);
    }
    
    public WalletNotFoundException() {
        super("Wallet not found");
    }
}

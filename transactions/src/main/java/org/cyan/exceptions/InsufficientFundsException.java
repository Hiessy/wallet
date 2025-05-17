package org.cyan.exceptions;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String insufficientFunds) {
        super(insufficientFunds);
    }
}

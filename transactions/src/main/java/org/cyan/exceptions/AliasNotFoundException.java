package org.cyan.exceptions;

public class AliasNotFoundException extends RuntimeException {
    public AliasNotFoundException(String message) {
        super(message);
    }
}

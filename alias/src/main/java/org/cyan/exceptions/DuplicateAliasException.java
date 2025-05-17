package org.cyan.exceptions;

public class DuplicateAliasException extends RuntimeException {
    public DuplicateAliasException(String aliasAlreadyExists) {
        super(aliasAlreadyExists);
    }
}

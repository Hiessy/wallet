package org.cyan.config;

import org.cyan.exceptions.AliasNotFoundException;
import org.cyan.exceptions.InsufficientFundsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        return new ResponseEntity<>(
                ErrorResponse.builder().error("Internal server error").message(ex.getMessage()).build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientFundsException(InsufficientFundsException ex) {

        System.out.println("Caught InsufficientFundsException: " + ex.getMessage());
        return new ResponseEntity<>(
                ErrorResponse.builder().error("Insufficient funds").message(ex.getMessage()).build(),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AliasNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAliasNotFoundException(AliasNotFoundException ex) {  // Corrected name

        System.out.println("Caught AliasNotFoundException: " + ex.getMessage());
        return new ResponseEntity<>(
                ErrorResponse.builder().error("Alias not found").message(ex.getMessage()).build(),
                HttpStatus.NOT_FOUND);
    }
}
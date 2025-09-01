package com.lsp.web.Exception;


public class InvalidInputException extends IllegalArgumentException {
    public InvalidInputException(String message) {
        super(message);
    }
}

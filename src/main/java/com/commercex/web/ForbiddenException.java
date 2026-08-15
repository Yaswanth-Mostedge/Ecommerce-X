package com.commercex.web;

/**
 * Raised when a signed-in user reaches another user's order.
 * Handled as a 403 rather than a server error.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}

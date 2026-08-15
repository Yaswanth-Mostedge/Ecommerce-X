package com.commercex.web;

/**
 * Raised when a storefront URL points at a product that does not exist
 * or is no longer on sale. Handled as a 404 rather than a server error.
 */
public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(String message) {
        super(message);
    }
}

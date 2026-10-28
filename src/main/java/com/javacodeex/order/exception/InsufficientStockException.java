package com.javacodeex.order.exception;

public class InsufficientStockException extends RuntimeException {

    /**
     * Creates an exception describing a missing product or insufficient stock.
     *
     * @param message explanation of why the order cannot be fulfilled
     */
    public InsufficientStockException(String message) {
        super(message);
    }
}

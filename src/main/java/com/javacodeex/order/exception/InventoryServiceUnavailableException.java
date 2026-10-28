package com.javacodeex.order.exception;

public class InventoryServiceUnavailableException extends RuntimeException {

    /**
     * Creates an inventory availability failure while retaining its original cause.
     *
     * @param message explanation of the inventory service failure
     * @param cause underlying failure that prevented the inventory check
     */
    public InventoryServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

package com.javacodeex.order.exception;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Converts a stock failure into an HTTP 409 response.
     *
     * @param exception missing-product or insufficient-stock failure
     * @param request HTTP request used to identify the failing URI
     * @return a conflict response containing the failure message
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException exception, HttpServletRequest request) {
        return response(HttpStatus.CONFLICT, "INSUFFICIENT_STOCK", exception.getMessage(), request);
    }

    /**
     * Converts an inventory availability failure into an HTTP 503 response.
     *
     * @param exception inventory service availability failure
     * @param request HTTP request used to identify the failing URI
     * @return a service-unavailable response containing the failure message
     */
    @ExceptionHandler(InventoryServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleInventoryUnavailable(
            InventoryServiceUnavailableException exception, HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", exception.getMessage(), request);
    }

    /**
     * Converts an unhandled Feign failure into an HTTP 503 response.
     *
     * @param exception upstream client failure
     * @param request HTTP request used to identify the failing URI
     * @return a service-unavailable response with a generic inventory error message
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignFailure(
            FeignException exception, HttpServletRequest request) {
        return response(HttpStatus.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE",
                "Inventory service is currently unavailable", request);
    }

    /**
     * Converts request-body validation errors into an HTTP 400 response using
     * the first field error, or a generic message when no field error is present.
     *
     * @param exception validation failure containing binding errors
     * @param request HTTP request used to identify the failing URI
     * @return a bad-request response describing the validation failure
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Request validation failed");
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    /**
     * Converts a constraint violation into an HTTP 400 response.
     *
     * @param exception constraint validation failure
     * @param request HTTP request used to identify the failing URI
     * @return a bad-request response containing the constraint violation message
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request) {
        return response(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", exception.getMessage(), request);
    }

    /**
     * Builds a consistent error body with the current local time and request URI.
     *
     * @param status HTTP status for both the response and error body
     * @param error machine-readable error code
     * @param message human-readable failure description
     * @param request HTTP request used to obtain the failing URI
     * @return the error body wrapped in a response with the supplied status
     */
    private ResponseEntity<ErrorResponse> response(HttpStatus status, String error,
                                                    String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(LocalDateTime.now(), status.value(), error, message, request.getRequestURI()));
    }

    /**
     * Describes a failed HTTP request.
     *
     * @param timestamp local date and time when the error response was created
     * @param status numeric HTTP status code
     * @param error machine-readable error code
     * @param message human-readable failure description
     * @param path URI of the failed request
     */
    public record ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
    }
}

package com.javacodeex.order.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceLoggingConfig {

    private static final Logger log = LoggerFactory.getLogger(ResilienceLoggingConfig.class);

    private final RetryRegistry retryRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ResilienceLoggingConfig(RetryRegistry retryRegistry,
                                   CircuitBreakerRegistry circuitBreakerRegistry) {
        this.retryRegistry = retryRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @PostConstruct
    void registerEventLogging() {
        Retry retry = retryRegistry.retry("inventory-service");
        retry.getEventPublisher()
                .onRetry(event -> log.warn(
                        "Retrying {}: attempt={} wait={}ms cause={}",
                        event.getName(),
                        event.getNumberOfRetryAttempts(),
                        event.getWaitInterval().toMillis(),
                        event.getLastThrowable().toString()))
                .onSuccess(event -> log.info(
                        "Retry {} succeeded after {} retry attempt(s)",
                        event.getName(), event.getNumberOfRetryAttempts()))
                .onError(event -> log.error(
                        "Retry {} exhausted after {} retry attempt(s): {}",
                        event.getName(),
                        event.getNumberOfRetryAttempts(),
                        event.getLastThrowable().toString()));

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("inventory-service");
        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn(
                        "Circuit breaker {} changed state from {} to {}",
                        event.getCircuitBreakerName(),
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()))
                .onCallNotPermitted(event -> log.warn(
                        "Circuit breaker {} rejected the inventory call because it is OPEN",
                        event.getCircuitBreakerName()));
    }
}

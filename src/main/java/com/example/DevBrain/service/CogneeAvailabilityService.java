package com.example.DevBrain.service;

import com.example.DevBrain.config.CogneeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.TimeoutException;

@Service
public class CogneeAvailabilityService {

    private static final Logger log = LoggerFactory.getLogger(CogneeAvailabilityService.class);

    private enum CircuitState {
        HEALTHY,
        OPEN,
        HALF_OPEN
    }

    private final int failureThreshold;
    private final Duration cooldown;
    private final Duration timeout;
    private final Clock clock;

    private final AtomicReference<CircuitState> state = new AtomicReference<>(CircuitState.HEALTHY);
    private final AtomicInteger consecutiveFailures = new AtomicInteger();
    private final AtomicLong openedAt = new AtomicLong(0L);
    private final AtomicBoolean fallbackLogged = new AtomicBoolean(false);

    @Autowired
    public CogneeAvailabilityService(CogneeProperties properties) {
        this(properties, Clock.systemUTC());
    }

    public CogneeAvailabilityService(CogneeProperties properties, Clock clock) {
        this.failureThreshold = properties.getFallback() != null ? properties.getFallback().getFailureThreshold() : 3;
        this.cooldown = properties.getFallback() != null ? properties.getFallback().getCooldown() : Duration.ofMinutes(1);
        this.timeout = properties.getFallback() != null ? properties.getFallback().getTimeout() : properties.getRequest().getTimeout();
        this.clock = clock;
    }

    public boolean canAttempt() {
        CircuitState currentState = state.get();
        if (currentState == CircuitState.HEALTHY) {
            return true;
        }

        if (currentState == CircuitState.OPEN) {
            long openedAtMillis = openedAt.get();
            if (openedAtMillis > 0 && Instant.ofEpochMilli(openedAtMillis).plus(cooldown).isBefore(Instant.now(clock))) {
                state.set(CircuitState.HALF_OPEN);
                return true;
            }
            return false;
        }

        return true;
    }

    public boolean shouldUseFallback(Throwable throwable) {
        if (!isTransientFailure(throwable)) {
            return false;
        }
        recordFailure(throwable);
        return !canAttempt();
    }

    public void recordSuccess() {
        if (consecutiveFailures.getAndSet(0) > 0 || state.get() != CircuitState.HEALTHY) {
            state.set(CircuitState.HEALTHY);
            openedAt.set(0L);
            fallbackLogged.set(false);
            log.info("Cognee recovered and the circuit breaker has been reset.");
        }
    }

    public void recordFailure(Throwable throwable) {
        if (!isTransientFailure(throwable)) {
            return;
        }

        if (state.get() == CircuitState.HALF_OPEN) {
            openCircuit();
            return;
        }

        int failures = consecutiveFailures.incrementAndGet();
        if (failures >= failureThreshold) {
            openCircuit();
        }
    }

    public String getCurrentStatus() {
        CircuitState currentState = state.get();
        if (currentState == CircuitState.OPEN) {
            return "UNAVAILABLE";
        }
        if (currentState == CircuitState.HALF_OPEN) {
            return "FALLBACK_ACTIVE";
        }
        return "HEALTHY";
    }

    public boolean isFallbackActive() {
        return state.get() != CircuitState.HEALTHY;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public int getFailureThreshold() {
        return failureThreshold;
    }

    public Duration getCooldown() {
        return cooldown;
    }

    private void openCircuit() {
        if (state.compareAndSet(CircuitState.HEALTHY, CircuitState.OPEN) || state.compareAndSet(CircuitState.HALF_OPEN, CircuitState.OPEN)) {
            openedAt.set(Instant.now(clock).toEpochMilli());
            consecutiveFailures.set(failureThreshold);
            if (fallbackLogged.compareAndSet(false, true)) {
                log.warn("Cognee fallback activated after repeated transient failures. Recovery will be retried after {}.", cooldown);
            }
        }
    }

    private boolean isTransientFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof WebClientResponseException webClientResponseException) {
                int status = webClientResponseException.getStatusCode().value();
                if (status == 429 || status >= 500) {
                    return true;
                }
            }

            String message = current.getMessage() == null ? "" : current.getMessage().toLowerCase(Locale.ROOT);
            if (current instanceof TimeoutException
                    || current instanceof SocketTimeoutException
                    || current instanceof ConnectException
                    || current instanceof UnknownHostException
                    || current instanceof SocketException && message.contains("reset")
                    || message.contains("connection reset")
                    || message.contains("timed out")
                    || message.contains("timeout")
                    || message.contains("connection refused")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}

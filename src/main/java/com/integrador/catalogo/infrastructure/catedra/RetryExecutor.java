package com.integrador.catalogo.infrastructure.catedra;

import com.integrador.catalogo.application.exception.CatedraUnavailableException;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Reintentos ACOTADOS con backoff exponencial. Solo reintenta errores transitorios
 * (CatedraUnavailableException); cualquier otro error se propaga de inmediato.
 */
public class RetryExecutor {

    private final int maxAttempts;
    private final Duration initialBackoff;

    public RetryExecutor(int maxAttempts, Duration initialBackoff) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts debe ser al menos 1");
        }
        this.maxAttempts = maxAttempts;
        this.initialBackoff = initialBackoff;
    }

    public <T> T execute(Supplier<T> action) {
        Duration wait = initialBackoff;
        for (int attempt = 1; ; attempt++) {
            try {
                return action.get();
            } catch (CatedraUnavailableException e) {
                if (attempt >= maxAttempts) {
                    throw e;
                }
                pause(wait);
                wait = wait.multipliedBy(2);
            }
        }
    }

    private void pause(Duration wait) {
        try {
            Thread.sleep(wait.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CatedraUnavailableException("Reintento interrumpido", 0, null, e);
        }
    }
}

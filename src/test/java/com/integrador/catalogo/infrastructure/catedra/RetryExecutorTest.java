package com.integrador.catalogo.infrastructure.catedra;

import com.integrador.catalogo.application.exception.CatedraAuthenticationException;
import com.integrador.catalogo.application.exception.CatedraUnavailableException;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RetryExecutorTest {

    private final RetryExecutor retry = new RetryExecutor(3, Duration.ZERO);

    @Test
    void reintentaErroresTransitoriosHastaTenerExito() {
        AtomicInteger calls = new AtomicInteger();

        String result = retry.execute(() -> {
            if (calls.incrementAndGet() < 3) {
                throw new CatedraUnavailableException("503", 503, null, null);
            }
            return "ok";
        });

        assertThat(result).isEqualTo("ok");
        assertThat(calls).hasValue(3);
    }

    @Test
    void noReintentaIndefinidamente() {
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> retry.execute(() -> {
            calls.incrementAndGet();
            throw new CatedraUnavailableException("503", 503, null, null);
        })).isInstanceOf(CatedraUnavailableException.class);

        assertThat(calls).hasValue(3);
    }

    @Test
    void noReintentaErroresDeAutenticacion() {
        AtomicInteger calls = new AtomicInteger();

        assertThatThrownBy(() -> retry.execute(() -> {
            calls.incrementAndGet();
            throw new CatedraAuthenticationException("401", 401, null, null);
        })).isInstanceOf(CatedraAuthenticationException.class);

        assertThat(calls).hasValue(1);
    }
}

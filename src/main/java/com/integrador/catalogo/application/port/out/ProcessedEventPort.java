package com.integrador.catalogo.application.port.out;

import java.time.Instant;

/** Registro de eventIds ya procesados: la clave de idempotencia de Kafka (entrega al menos una vez). */
public interface ProcessedEventPort {

    boolean wasProcessed(String eventId);

    void markProcessed(String eventId, Instant at);
}

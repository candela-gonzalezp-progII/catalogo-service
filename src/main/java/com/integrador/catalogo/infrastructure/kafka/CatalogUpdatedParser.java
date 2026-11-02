package com.integrador.catalogo.infrastructure.kafka;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.integrador.catalogo.domain.model.CatalogUpdatedEvent;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.Optional;

/** Convierte el JSON de Kafka en un CatalogUpdatedEvent. Ignora campos y tipos de evento desconocidos. */
public class CatalogUpdatedParser {

    private static final String EVENT_TYPE = "CatalogUpdated";
    private final JsonMapper json = JsonMapper.builder().build();

    /** @return vacio si no es un CatalogUpdated; lanza IllegalArgumentException si esta mal formado. */
    public Optional<CatalogUpdatedEvent> parse(String value) {
        Message message;
        try {
            message = json.readValue(value, Message.class);
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("Mensaje de catalogo con JSON invalido");
        }
        if (message == null || !EVENT_TYPE.equals(message.eventType())) {
            return Optional.empty();
        }
        if (message.newVersion() == null) {
            throw new IllegalArgumentException("CatalogUpdated sin newVersion");
        }
        return Optional.of(new CatalogUpdatedEvent(message.eventId(), message.newVersion(),
                message.occurredAt(), message.source()));
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Message(String eventId, String eventType, Long newVersion, Instant occurredAt, String source) {
    }
}

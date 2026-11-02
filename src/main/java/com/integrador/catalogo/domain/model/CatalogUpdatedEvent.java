package com.integrador.catalogo.domain.model;

import java.time.Instant;

/** Evento CatalogUpdated de la catedra: avisa que existe una nueva version del catalogo (los datos no vienen en el evento). */
public record CatalogUpdatedEvent(String eventId, long newVersion, Instant occurredAt, String source) {

    public CatalogUpdatedEvent {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId es obligatorio");
        }
        if (newVersion < 0) {
            throw new IllegalArgumentException("newVersion no puede ser negativa");
        }
    }
}

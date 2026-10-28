package com.integrador.catalogo.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/** Foto completa del catalogo de la catedra en una version determinada. */
public record CatalogSnapshot(
        long version,
        Instant generatedAt,
        List<ProfessionalCategory> categories,
        List<Professional> professionals,
        List<WeeklySchedule> schedules) {

    public CatalogSnapshot {
        Objects.requireNonNull(generatedAt, "generatedAt es obligatorio");
        categories = List.copyOf(categories);
        professionals = List.copyOf(professionals);
        schedules = List.copyOf(schedules);
    }
}

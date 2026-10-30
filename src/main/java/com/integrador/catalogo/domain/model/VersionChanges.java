package com.integrador.catalogo.domain.model;

import java.util.List;

/** IDs afectados por una version del catalogo (no incluye las entidades completas). */
public record VersionChanges(
        long version,
        List<Long> categoryIds,
        List<Long> professionalIds,
        List<Long> scheduleIds) {

    public VersionChanges {
        categoryIds = List.copyOf(categoryIds);
        professionalIds = List.copyOf(professionalIds);
        scheduleIds = List.copyOf(scheduleIds);
    }
}

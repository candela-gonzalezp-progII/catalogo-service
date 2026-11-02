package com.integrador.catalogo.domain.model;

import java.util.List;

/** Estado vigente de las entidades afectadas por UNA version del catalogo. */
public record VersionData(
        long version,
        List<ProfessionalCategory> categories,
        List<Professional> professionals,
        List<WeeklySchedule> schedules) {

    public VersionData {
        categories = List.copyOf(categories);
        professionals = List.copyOf(professionals);
        schedules = List.copyOf(schedules);
    }
}

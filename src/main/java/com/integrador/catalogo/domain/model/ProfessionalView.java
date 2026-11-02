package com.integrador.catalogo.domain.model;

import java.util.List;

/** Un profesional junto con su categoria y sus horarios semanales (lectura). */
public record ProfessionalView(Professional professional, ProfessionalCategory category, List<WeeklySchedule> schedules) {

    public ProfessionalView {
        schedules = List.copyOf(schedules);
    }
}

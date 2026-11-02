package com.integrador.catalogo.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Filtros de busqueda de profesionales (CU-08). Todos son opcionales.
 * Se validan al construirse: un filtro invalido nunca llega a la base.
 *
 * @param categoryId filtra por categoria
 * @param name       texto contenido en "nombre apellido" (sin distinguir mayusculas)
 * @param enabled    true = solo habilitados, false = solo deshabilitados, null = todos
 * @param day        dia de la semana en que atiende
 * @param time       si se informa junto con day, que atienda a esa hora
 */
public record ProfessionalSearchCriteria(
        Long categoryId,
        String name,
        Boolean enabled,
        DayOfWeek day,
        LocalTime time,
        int page,
        int size) {

    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_PAGE_SIZE = 20;

    public ProfessionalSearchCriteria {
        if (page < 0) {
            throw new IllegalArgumentException("page no puede ser negativo");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("size debe estar entre 1 y " + MAX_PAGE_SIZE);
        }
        if (time != null && day == null) {
            throw new IllegalArgumentException("time requiere informar tambien day");
        }
        name = (name == null || name.isBlank()) ? null : name.trim();
    }

    public static ProfessionalSearchCriteria all() {
        return new ProfessionalSearchCriteria(null, null, null, null, null, 0, DEFAULT_PAGE_SIZE);
    }
}

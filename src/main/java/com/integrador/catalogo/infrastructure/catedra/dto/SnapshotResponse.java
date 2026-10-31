package com.integrador.catalogo.infrastructure.catedra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.integrador.catalogo.application.exception.CatedraIntegrationException;
import com.integrador.catalogo.domain.model.CatalogSnapshot;
import com.integrador.catalogo.domain.model.Professional;
import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.domain.model.WeeklySchedule;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

/** Respuesta de GET /api/synchronization/snapshot (contrato del anexo, seccion 7). */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SnapshotResponse(
        Long snapshotVersion,
        Instant generatedAt,
        List<CategoryItem> professionalCategories,
        List<ProfessionalItem> professionals,
        List<ScheduleItem> weeklySchedules) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CategoryItem(Long id, String name, String description, Boolean enabled) {
        ProfessionalCategory toDomain() {
            return new ProfessionalCategory(id, name, description, isTrue(enabled));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ProfessionalItem(Long id, Long categoryId, String firstName, String lastName, Boolean enabled) {
        Professional toDomain() {
            return new Professional(id, firstName, lastName, categoryId, isTrue(enabled));
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ScheduleItem(Long id, Long professionalId, DayOfWeek dayOfWeek, LocalTime startTime,
                               LocalTime endTime, Integer slotDurationMinutes, Boolean enabled) {
        WeeklySchedule toDomain() {
            int slot = slotDurationMinutes != null ? slotDurationMinutes : 0;
            return new WeeklySchedule(id, professionalId, dayOfWeek, startTime, endTime, slot, isTrue(enabled));
        }
    }

    /**
     * Convierte el DTO a dominio. Si el snapshot trae datos invalidos se rechaza completo
     * ANTES de tocar la base local.
     */
    public CatalogSnapshot toDomain() {
        if (snapshotVersion == null || generatedAt == null) {
            throw new CatedraIntegrationException("Snapshot invalido: faltan snapshotVersion o generatedAt");
        }
        try {
            return new CatalogSnapshot(snapshotVersion, generatedAt,
                    orEmpty(professionalCategories).stream().map(CategoryItem::toDomain).toList(),
                    orEmpty(professionals).stream().map(ProfessionalItem::toDomain).toList(),
                    orEmpty(weeklySchedules).stream().map(ScheduleItem::toDomain).toList());
        } catch (IllegalArgumentException e) {
            throw new CatedraIntegrationException("Snapshot invalido: " + e.getMessage());
        }
    }

    private static <T> List<T> orEmpty(List<T> list) {
        return Objects.requireNonNullElse(list, List.of());
    }

    private static boolean isTrue(Boolean value) {
        return Boolean.TRUE.equals(value);
    }
}

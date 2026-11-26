package com.integrador.catalogo.infrastructure.web;

import com.integrador.catalogo.domain.model.ProfessionalView;
import com.integrador.catalogo.domain.model.WeeklySchedule;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/** Contrato publico del profesional. No expone clases de dominio ni de JPA. */
public record ProfessionalResponse(
        Long id,
        String firstName,
        String lastName,
        String fullName,
        boolean enabled,
        CategoryResponse category,
        List<ScheduleResponse> weeklySchedules) {

    public record CategoryResponse(Long id, String name) {
    }

    public record ScheduleResponse(Long id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
                                   int slotDurationMinutes, boolean enabled) {
        static ScheduleResponse from(WeeklySchedule s) {
            return new ScheduleResponse(s.getId(), s.getDayOfWeek(), s.getStartTime(), s.getEndTime(),
                    s.getSlotDurationMinutes(), s.isActive());
        }
    }

    public static ProfessionalResponse from(ProfessionalView view) {
        var p = view.professional();
        var category = view.category() == null ? null
                : new CategoryResponse(view.category().getId(), view.category().getName());
        return new ProfessionalResponse(p.getId(), p.getFirstName(), p.getLastName(), p.fullName(), p.isActive(),
                category, view.schedules().stream().map(ScheduleResponse::from).toList());
    }
}

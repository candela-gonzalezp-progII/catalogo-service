package com.integrador.catalogo.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Bloque de horario semanal recurrente de un profesional.
 * Representa, por ejemplo: "Lunes de 09:00 a 13:00, turnos de 30 minutos".
 */
public class WeeklySchedule {

    private final Long id;
    private final Long professionalId;
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int slotDurationMinutes;
    private final boolean active;

    public WeeklySchedule(Long id, Long professionalId, DayOfWeek dayOfWeek,
                          LocalTime startTime, LocalTime endTime,
                          int slotDurationMinutes, boolean active) {
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser null");
        }
        if (professionalId == null) {
            throw new IllegalArgumentException("professionalId no puede ser null");
        }
        if (dayOfWeek == null) {
            throw new IllegalArgumentException("dayOfWeek no puede ser null");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("startTime y endTime son obligatorios");
        }
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime debe ser anterior a endTime");
        }
        if (slotDurationMinutes <= 0) {
            throw new IllegalArgumentException("slotDurationMinutes debe ser mayor a 0");
        }
        this.id = id;
        this.professionalId = professionalId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public Long getProfessionalId() {
        return professionalId;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getSlotDurationMinutes() {
        return slotDurationMinutes;
    }

    public boolean isActive() {
        return active;
    }
}

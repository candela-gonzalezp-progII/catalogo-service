package com.integrador.catalogo.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Bloque de horario semanal recurrente de un profesional.
 * Representa, por ejemplo: "Lunes de 09:00 a 13:00".
 */
public class WeeklySchedule {

    private final Long id;
    private final Long professionalId;
    private final DayOfWeek dayOfWeek;
    private final LocalTime startTime;
    private final LocalTime endTime;

    public WeeklySchedule(Long id, Long professionalId, DayOfWeek dayOfWeek,
                           LocalTime startTime, LocalTime endTime) {
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
        this.id = id;
        this.professionalId = professionalId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
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
}

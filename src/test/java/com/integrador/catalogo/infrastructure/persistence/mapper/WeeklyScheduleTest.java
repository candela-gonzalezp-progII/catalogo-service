package com.integrador.catalogo.infrastructure.persistence.mapper;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyScheduleTest {

    @Test
    void noPermiteStartTimePosteriorOIgualAEndTime() {
        assertThatThrownBy(() ->
                new WeeklySchedule(1L, 10L, DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(9, 0), 30, true)
        ).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("startTime");
    }

    @Test
    void noPermiteDuracionDeTurnoNulaONegativa() {
        assertThatThrownBy(() ->
                new WeeklySchedule(1L, 10L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0), 0, true)
        ).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("slotDurationMinutes");
    }

    @Test
    void permiteUnRangoValido() {
        assertThatCode(() ->
                new WeeklySchedule(1L, 10L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0), 30, true)
        ).doesNotThrowAnyException();
    }
}

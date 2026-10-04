package com.integrador.catalogo.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeeklyScheduleTest {

    @Test
    void noPermiteStartTimePosteriorOIgualAEndTime() {
        assertThatThrownBy(() ->
                new WeeklySchedule(1L, 10L, DayOfWeek.MONDAY, LocalTime.of(13, 0), LocalTime.of(9, 0))
        ).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("startTime");
    }

    @Test
    void permiteUnRangoValido() {
        assertThatCode(() ->
                new WeeklySchedule(1L, 10L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(13, 0))
        ).doesNotThrowAnyException();
    }
}

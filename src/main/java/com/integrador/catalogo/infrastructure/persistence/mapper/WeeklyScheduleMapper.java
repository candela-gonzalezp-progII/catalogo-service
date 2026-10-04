package com.integrador.catalogo.infrastructure.persistence.mapper;

import com.integrador.catalogo.domain.model.WeeklySchedule;
import com.integrador.catalogo.infrastructure.persistence.entity.WeeklyScheduleEntity;

public final class WeeklyScheduleMapper {

    private WeeklyScheduleMapper() {
    }

    public static WeeklyScheduleEntity toEntity(WeeklySchedule domain) {
        return new WeeklyScheduleEntity(
                domain.getId(), domain.getProfessionalId(), domain.getDayOfWeek(),
                domain.getStartTime(), domain.getEndTime()
        );
    }

    public static WeeklySchedule toDomain(WeeklyScheduleEntity entity) {
        return new WeeklySchedule(
                entity.getId(), entity.getProfessionalId(), entity.getDayOfWeek(),
                entity.getStartTime(), entity.getEndTime()
        );
    }
}

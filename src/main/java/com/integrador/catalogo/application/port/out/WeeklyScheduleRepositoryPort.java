package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.WeeklySchedule;

import java.util.List;

public interface WeeklyScheduleRepositoryPort {

    WeeklySchedule save(WeeklySchedule schedule);

    List<WeeklySchedule> findByProfessionalId(Long professionalId);

    void deleteAll();
}

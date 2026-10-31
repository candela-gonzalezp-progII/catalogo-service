package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.application.port.out.WeeklyScheduleRepositoryPort;
import com.integrador.catalogo.domain.model.WeeklySchedule;
import com.integrador.catalogo.infrastructure.persistence.jpa.WeeklyScheduleJpaRepository;
import com.integrador.catalogo.infrastructure.persistence.mapper.WeeklyScheduleMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WeeklyScheduleRepositoryAdapter implements WeeklyScheduleRepositoryPort {

    private final WeeklyScheduleJpaRepository jpaRepository;

    public WeeklyScheduleRepositoryAdapter(WeeklyScheduleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WeeklySchedule save(WeeklySchedule schedule) {
        var saved = jpaRepository.save(WeeklyScheduleMapper.toEntity(schedule));
        return WeeklyScheduleMapper.toDomain(saved);
    }

    @Override
    public List<WeeklySchedule> findByProfessionalId(Long professionalId) {
        return jpaRepository.findByProfessionalId(professionalId).stream()
                .map(WeeklyScheduleMapper::toDomain).toList();
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}

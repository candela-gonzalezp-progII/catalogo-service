package com.integrador.catalogo.infrastructure.persistence.jpa;

import com.integrador.catalogo.infrastructure.persistence.entity.WeeklyScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeeklyScheduleJpaRepository extends JpaRepository<WeeklyScheduleEntity, Long> {

    List<WeeklyScheduleEntity> findByProfessionalId(Long professionalId);
}

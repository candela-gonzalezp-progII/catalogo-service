package com.integrador.catalogo.infrastructure.persistence.jpa;

import com.integrador.catalogo.infrastructure.persistence.entity.ProfessionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProfessionalJpaRepository extends JpaRepository<ProfessionalEntity, Long> {

    List<ProfessionalEntity> findByCategoryId(Long categoryId);
}

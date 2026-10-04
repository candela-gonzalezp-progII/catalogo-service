package com.integrador.catalogo.infrastructure.persistence.jpa;

import com.integrador.catalogo.infrastructure.persistence.entity.ProfessionalCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfessionalCategoryJpaRepository extends JpaRepository<ProfessionalCategoryEntity, Long> {
}

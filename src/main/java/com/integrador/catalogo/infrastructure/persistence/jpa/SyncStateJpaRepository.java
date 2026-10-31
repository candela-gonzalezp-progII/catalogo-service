package com.integrador.catalogo.infrastructure.persistence.jpa;

import com.integrador.catalogo.infrastructure.persistence.entity.SyncStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SyncStateJpaRepository extends JpaRepository<SyncStateEntity, Long> {
}

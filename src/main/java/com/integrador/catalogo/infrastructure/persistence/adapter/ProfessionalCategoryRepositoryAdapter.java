package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.application.port.out.ProfessionalCategoryRepositoryPort;
import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.infrastructure.persistence.jpa.ProfessionalCategoryJpaRepository;
import com.integrador.catalogo.infrastructure.persistence.mapper.ProfessionalCategoryMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Adaptador de salida: implementa el puerto usando Spring Data JPA.
 * El resto de la aplicacion solo conoce ProfessionalCategoryRepositoryPort (DIP).
 */
@Component
public class ProfessionalCategoryRepositoryAdapter implements ProfessionalCategoryRepositoryPort {

    private final ProfessionalCategoryJpaRepository jpaRepository;

    public ProfessionalCategoryRepositoryAdapter(ProfessionalCategoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ProfessionalCategory save(ProfessionalCategory category) {
        var saved = jpaRepository.save(ProfessionalCategoryMapper.toEntity(category));
        return ProfessionalCategoryMapper.toDomain(saved);
    }

    @Override
    public Optional<ProfessionalCategory> findById(Long id) {
        return jpaRepository.findById(id).map(ProfessionalCategoryMapper::toDomain);
    }

    @Override
    public List<ProfessionalCategory> findAll() {
        return jpaRepository.findAll().stream().map(ProfessionalCategoryMapper::toDomain).toList();
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}

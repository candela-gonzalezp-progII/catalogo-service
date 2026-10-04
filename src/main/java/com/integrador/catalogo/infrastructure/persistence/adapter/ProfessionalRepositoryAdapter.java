package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.application.port.out.ProfessionalRepositoryPort;
import com.integrador.catalogo.domain.model.Professional;
import com.integrador.catalogo.infrastructure.persistence.jpa.ProfessionalJpaRepository;
import com.integrador.catalogo.infrastructure.persistence.mapper.ProfessionalMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ProfessionalRepositoryAdapter implements ProfessionalRepositoryPort {

    private final ProfessionalJpaRepository jpaRepository;

    public ProfessionalRepositoryAdapter(ProfessionalJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Professional save(Professional professional) {
        var saved = jpaRepository.save(ProfessionalMapper.toEntity(professional));
        return ProfessionalMapper.toDomain(saved);
    }

    @Override
    public Optional<Professional> findById(Long id) {
        return jpaRepository.findById(id).map(ProfessionalMapper::toDomain);
    }

    @Override
    public List<Professional> findByCategoryId(Long categoryId) {
        return jpaRepository.findByCategoryId(categoryId).stream().map(ProfessionalMapper::toDomain).toList();
    }

    @Override
    public List<Professional> findAll() {
        return jpaRepository.findAll().stream().map(ProfessionalMapper::toDomain).toList();
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}

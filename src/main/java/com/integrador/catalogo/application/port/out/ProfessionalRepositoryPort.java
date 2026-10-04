package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.Professional;

import java.util.List;
import java.util.Optional;

public interface ProfessionalRepositoryPort {

    Professional save(Professional professional);

    Optional<Professional> findById(Long id);

    List<Professional> findByCategoryId(Long categoryId);

    List<Professional> findAll();

    void deleteAll();
}

package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.ProfessionalCategory;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida: lo que la aplicacion necesita para persistir categorias.
 * El dominio/aplicacion depende de esta interfaz, nunca de JPA directamente (DIP).
 */
public interface ProfessionalCategoryRepositoryPort {

    ProfessionalCategory save(ProfessionalCategory category);

    Optional<ProfessionalCategory> findById(Long id);

    List<ProfessionalCategory> findAll();

    void deleteAll();
}

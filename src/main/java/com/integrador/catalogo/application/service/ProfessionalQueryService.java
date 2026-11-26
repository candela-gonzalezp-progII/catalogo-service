package com.integrador.catalogo.application.service;

import com.integrador.catalogo.application.exception.ProfessionalNotFoundException;
import com.integrador.catalogo.application.port.in.GetProfessionalUseCase;
import com.integrador.catalogo.application.port.in.ListCategoriesUseCase;
import com.integrador.catalogo.application.port.in.SearchProfessionalsUseCase;
import com.integrador.catalogo.application.port.out.ProfessionalCategoryRepositoryPort;
import com.integrador.catalogo.application.port.out.ProfessionalSearchPort;
import com.integrador.catalogo.domain.model.PageResult;
import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.domain.model.ProfessionalSearchCriteria;
import com.integrador.catalogo.domain.model.ProfessionalView;

import java.util.Comparator;
import java.util.List;

/**
 * CU-08. Todas las consultas se resuelven con datos locales: este servicio no conoce
 * ningun puerto de la catedra, por lo que no puede llamarla (regla del enunciado).
 */
public class ProfessionalQueryService
        implements SearchProfessionalsUseCase, GetProfessionalUseCase, ListCategoriesUseCase {

    private final ProfessionalSearchPort searchPort;
    private final ProfessionalCategoryRepositoryPort categoryPort;

    public ProfessionalQueryService(ProfessionalSearchPort searchPort, ProfessionalCategoryRepositoryPort categoryPort) {
        this.searchPort = searchPort;
        this.categoryPort = categoryPort;
    }

    @Override
    public PageResult<ProfessionalView> search(ProfessionalSearchCriteria criteria) {
        return searchPort.search(criteria);
    }

    @Override
    public ProfessionalView getById(Long id) {
        return searchPort.findViewById(id).orElseThrow(() -> new ProfessionalNotFoundException(id));
    }

    @Override
    public List<ProfessionalCategory> listAll() {
        return categoryPort.findAll().stream()
                .sorted(Comparator.comparing(ProfessionalCategory::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }
}

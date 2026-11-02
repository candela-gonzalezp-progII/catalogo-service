package com.integrador.catalogo.application.port.in;

import com.integrador.catalogo.domain.model.ProfessionalCategory;

import java.util.List;

public interface ListCategoriesUseCase {

    List<ProfessionalCategory> listAll();
}

package com.integrador.catalogo.infrastructure.web;

import com.integrador.catalogo.domain.model.ProfessionalCategory;

public record CategoryResponse(Long id, String name, String description, boolean enabled) {

    public static CategoryResponse from(ProfessionalCategory c) {
        return new CategoryResponse(c.getId(), c.getName(), c.getDescription(), c.isActive());
    }
}

package com.integrador.catalogo.infrastructure.persistence.mapper;

import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.infrastructure.persistence.entity.ProfessionalCategoryEntity;

public final class ProfessionalCategoryMapper {

    private ProfessionalCategoryMapper() {
    }

    public static ProfessionalCategoryEntity toEntity(ProfessionalCategory domain) {
        return new ProfessionalCategoryEntity(
                domain.getId(), domain.getName(), domain.getDescription(), domain.isActive()
        );
    }

    public static ProfessionalCategory toDomain(ProfessionalCategoryEntity entity) {
        return new ProfessionalCategory(
                entity.getId(), entity.getName(), entity.getDescription(), entity.isActive()
        );
    }
}

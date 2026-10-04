package com.integrador.catalogo.infrastructure.persistence.mapper;

import com.integrador.catalogo.domain.model.Professional;
import com.integrador.catalogo.infrastructure.persistence.entity.ProfessionalEntity;

public final class ProfessionalMapper {

    private ProfessionalMapper() {
    }

    public static ProfessionalEntity toEntity(Professional domain) {
        return new ProfessionalEntity(
                domain.getId(), domain.getFirstName(), domain.getLastName(),
                domain.getCategoryId(), domain.isActive()
        );
    }

    public static Professional toDomain(ProfessionalEntity entity) {
        return new Professional(
                entity.getId(), entity.getFirstName(), entity.getLastName(),
                entity.getCategoryId(), entity.isActive()
        );
    }
}

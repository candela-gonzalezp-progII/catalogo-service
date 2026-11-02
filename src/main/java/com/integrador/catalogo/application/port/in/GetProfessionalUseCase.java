package com.integrador.catalogo.application.port.in;

import com.integrador.catalogo.domain.model.ProfessionalView;

/** Detalle de un profesional con sus reglas de agenda (lo consume turnos-service). */
public interface GetProfessionalUseCase {

    /** @throws com.integrador.catalogo.application.exception.ProfessionalNotFoundException si no existe */
    ProfessionalView getById(Long id);
}

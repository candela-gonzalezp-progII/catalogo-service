package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.IntegrationConfig;
import com.integrador.catalogo.domain.model.TechnicalSession;

/** CU-04: autenticar la cuenta tecnica y recuperar la configuracion asignada por la catedra. */
public interface CatedraAuthPort {

    /** POST /api/authenticate */
    TechnicalSession authenticate();

    /** GET /api/student/integration */
    IntegrationConfig fetchIntegration();
}

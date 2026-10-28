package com.integrador.catalogo.infrastructure.catedra;

import com.integrador.catalogo.application.port.out.CatedraAuthPort;
import com.integrador.catalogo.domain.model.IntegrationConfig;
import com.integrador.catalogo.domain.model.TechnicalSession;
import com.integrador.catalogo.infrastructure.catedra.dto.IntegrationItem;

/** Adaptador de salida de CU-04: habla con /api/authenticate y /api/student/integration. */
public class CatedraAuthAdapter implements CatedraAuthPort {

    private final CatedraTokenProvider tokens;
    private final CatedraApiClient api;

    public CatedraAuthAdapter(CatedraTokenProvider tokens, CatedraApiClient api) {
        this.tokens = tokens;
        this.api = api;
    }

    @Override
    public TechnicalSession authenticate() {
        return tokens.authenticate();
    }

    @Override
    public IntegrationConfig fetchIntegration() {
        return api.get("/api/student/integration", IntegrationItem.class).toDomain();
    }
}

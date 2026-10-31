package com.integrador.catalogo.infrastructure.catedra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.integrador.catalogo.domain.model.TechnicalSession;

/** Respuesta de POST /api/student/register y POST /api/authenticate. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthResponse(@JsonProperty("id_token") String idToken, IntegrationItem integration) {

    public TechnicalSession toSession() {
        return new TechnicalSession(idToken, integration != null ? integration.toDomain() : null);
    }

    @Override
    public String toString() {
        return "AuthResponse[idToken=***, integration=" + integration + "]";
    }
}

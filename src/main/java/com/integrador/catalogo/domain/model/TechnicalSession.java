package com.integrador.catalogo.domain.model;

/** JWT tecnico de la catedra + la configuracion de integracion. El token nunca se imprime. */
public record TechnicalSession(String token, IntegrationConfig integration) {

    @Override
    public String toString() {
        return "TechnicalSession[token=***, integration=" + integration + "]";
    }
}

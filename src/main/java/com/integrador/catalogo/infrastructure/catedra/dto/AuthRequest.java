package com.integrador.catalogo.infrastructure.catedra.dto;

/** Body de POST /api/authenticate. rememberMe=true pide la vigencia larga. */
public record AuthRequest(String username, String password, boolean rememberMe) {

    @Override
    public String toString() {
        return "AuthRequest[username=" + username + ", password=***]";
    }
}

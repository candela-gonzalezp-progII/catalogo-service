package com.integrador.catalogo.infrastructure.security;

/**
 * Reglas de acceso del catalogo en un solo lugar (puro, sin Spring).
 * - /api/sync/**  -> SOLO servicios (uso interno; no es para la app).
 * - el resto de /api -> usuario final (la app) o servicio (turnos).
 * Lo no previsto se cierra: hace falta al menos uno de esos roles.
 */
public class AccessPolicy {

    static final String USER = "ROLE_USER";
    static final String ADMIN = "ROLE_ADMIN";
    static final String SERVICE = "ROLE_SERVICE";

    public Requirement requirementFor(String method, String path) {
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return Requirement.publicAccess();
        }
        if (path.equals("/api/sync") || path.startsWith("/api/sync/")) {
            return Requirement.anyOf(SERVICE);
        }
        return Requirement.anyOf(USER, ADMIN, SERVICE);
    }
}

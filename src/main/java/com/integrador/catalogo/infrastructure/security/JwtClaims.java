package com.integrador.catalogo.infrastructure.security;

import java.time.Instant;
import java.util.Set;

/**
 * Contenido del token.
 *
 * @param subject     login del usuario, o el nombre del servicio si es un token tecnico
 * @param authorities ROLE_USER / ROLE_ADMIN (usuarios) o ROLE_SERVICE (servicio a servicio)
 * @param onBehalfOf  en un token de servicio, el usuario final en cuyo nombre se llama (puede ser null)
 */
public record JwtClaims(String subject, Set<String> authorities, String issuer, Instant issuedAt,
                        Instant expiresAt, String onBehalfOf) {

    public JwtClaims {
        authorities = Set.copyOf(authorities);
    }
}

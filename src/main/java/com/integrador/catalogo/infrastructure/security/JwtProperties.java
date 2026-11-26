package com.integrador.catalogo.infrastructure.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * security.jwt.secret -> JWT_SECRET del .env (NUNCA en el repo; IDENTICO al de turnos-service)
 * security.jwt.issuer -> quien emite los tokens que el catalogo acepta (turnos-service)
 */
@ConfigurationProperties(prefix = "security.jwt")
public record JwtProperties(String secret, String issuer) {

    public JwtProperties {
        issuer = issuer == null || issuer.isBlank() ? "turnos-service" : issuer;
    }
}

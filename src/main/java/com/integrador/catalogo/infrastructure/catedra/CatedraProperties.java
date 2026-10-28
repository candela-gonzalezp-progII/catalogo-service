package com.integrador.catalogo.infrastructure.catedra;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * Configuracion de la integracion con la catedra. Todo viene de variables de entorno / .env:
 * nunca se escriben credenciales en el codigo.
 */
@ConfigurationProperties(prefix = "catedra")
public record CatedraProperties(
        String baseUrl,
        String username,
        String password,
        @DefaultValue("3s") Duration connectTimeout,
        @DefaultValue("10s") Duration readTimeout,
        @DefaultValue("3") int maxAttempts,
        @DefaultValue("500ms") Duration retryBackoff,
        @DefaultValue("false") boolean syncOnStartup) {

    @Override
    public String toString() {
        return "CatedraProperties[baseUrl=" + baseUrl + ", username=" + username + ", password=***]";
    }
}

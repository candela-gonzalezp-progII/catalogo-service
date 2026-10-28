package com.integrador.catalogo.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Seguridad: ni el token tecnico ni la clave de Redis pueden aparecer en un log. */
class SecretsNotLoggedTest {

    private final IntegrationConfig config = new IntegrationConfig("grupo", "host", 6379, "usuario",
            "CLAVE-REDIS", "r", "w", "kafka:9092", "cg", "t1", "t2", "t3", ProvisioningStatus.PROVISIONED);

    @Test
    void laConfiguracionOcultaLaClaveDeRedis() {
        assertThat(config.toString()).doesNotContain("CLAVE-REDIS");
    }

    @Test
    void laSesionOcultaElToken() {
        assertThat(new TechnicalSession("JWT-SECRETO", config).toString()).doesNotContain("JWT-SECRETO");
    }
}

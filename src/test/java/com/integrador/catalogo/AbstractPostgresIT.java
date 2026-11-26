package com.integrador.catalogo;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base de los tests de integracion con Postgres REAL (Testcontainers), nunca H2.
 * El contenedor se levanta una sola vez y lo comparten todas las clases que heredan de esta.
 */
@SpringBootTest
public abstract class AbstractPostgresIT {

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        // Secreto SOLO para tests (el real viene del .env y nunca se sube al repo)
        registry.add("security.jwt.secret", () -> "secreto-solo-para-tests-de-integracion-0123456789");
    }
}

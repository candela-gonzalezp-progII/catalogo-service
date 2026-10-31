package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.domain.model.ProfessionalCategory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de integracion real contra Postgres (Testcontainers), nunca H2,
 * para respetar la consigna de no usar bases de datos embebidas.
 */
@SpringBootTest
@Testcontainers
class ProfessionalCategoryRepositoryAdapterIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProfessionalCategoryRepositoryAdapter adapter;

    @Test
    void guardaYRecuperaUnaCategoriaPorId() {
        ProfessionalCategory saved = adapter.save(
                new ProfessionalCategory(1L, "Medicina General", "Consultas generales", true)
        );

        var found = adapter.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Medicina General");
    }
}

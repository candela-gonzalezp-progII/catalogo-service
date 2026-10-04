package com.integrador.catalogo.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfessionalTest {

    @Test
    void fullNameConcatenaNombreYApellido() {
        Professional professional = new Professional(1L, "Ana", "Perez", 10L, true);

        assertThat(professional.fullName()).isEqualTo("Ana Perez");
    }

    @Test
    void noPermiteCategoryIdNulo() {
        assertThatThrownBy(() -> new Professional(1L, "Ana", "Perez", null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("categoryId");
    }

    @Test
    void noPermiteFirstNameVacio() {
        assertThatThrownBy(() -> new Professional(1L, " ", "Perez", 10L, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("firstName");
    }

    @Test
    void deactivateCambiaElEstadoAInactivo() {
        Professional professional = new Professional(1L, "Ana", "Perez", 10L, true);

        professional.deactivate();

        assertThat(professional.isActive()).isFalse();
    }
}

package com.integrador.catalogo.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfessionalSearchCriteriaTest {

    @Test
    void sinFiltrosEsValido() {
        assertThat(ProfessionalSearchCriteria.all().size()).isEqualTo(ProfessionalSearchCriteria.DEFAULT_PAGE_SIZE);
    }

    @Test
    void laHoraRequiereElDia() {
        assertThatThrownBy(() ->
                new ProfessionalSearchCriteria(null, null, null, null, LocalTime.of(9, 0), 0, 20)
        ).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("day");
    }

    @Test
    void rechazaPaginasYTamaniosInvalidos() {
        assertThatThrownBy(() -> new ProfessionalSearchCriteria(null, null, null, null, null, -1, 20))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProfessionalSearchCriteria(null, null, null, null, null, 0, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ProfessionalSearchCriteria(null, null, null, null, null, 0, 101))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void unNombreEnBlancoSeIgnora() {
        var criteria = new ProfessionalSearchCriteria(null, "   ", null, DayOfWeek.MONDAY, null, 0, 10);

        assertThat(criteria.name()).isNull();
    }

    @Test
    void elNombreSeRecorta() {
        var criteria = new ProfessionalSearchCriteria(null, "  ana ", null, null, null, 0, 10);

        assertThat(criteria.name()).isEqualTo("ana");
    }
}

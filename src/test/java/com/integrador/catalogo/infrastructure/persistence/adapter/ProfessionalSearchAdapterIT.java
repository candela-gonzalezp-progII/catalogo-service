package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.AbstractPostgresIT;
import com.integrador.catalogo.application.port.out.CatalogReplicaPort;
import com.integrador.catalogo.application.port.out.ProfessionalSearchPort;
import com.integrador.catalogo.domain.model.CatalogSnapshot;
import com.integrador.catalogo.domain.model.Professional;
import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.domain.model.ProfessionalSearchCriteria;
import com.integrador.catalogo.domain.model.ProfessionalView;
import com.integrador.catalogo.domain.model.WeeklySchedule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CU-08 contra Postgres real: un test por filtro obligatorio (categoria, nombre, habilitado,
 * disponibilidad) y por combinaciones. Datos:
 *   Ana Gomez   (cat 1, habilitada)    lunes 09-12 y miercoles 14-18
 *   Luis Perez  (cat 1, deshabilitado) lunes 09-12
 *   Marta Ruiz  (cat 2, habilitada)    martes 10-13 (horario deshabilitado)
 */
class ProfessionalSearchAdapterIT extends AbstractPostgresIT {

    @Autowired
    private CatalogReplicaPort replica;
    @Autowired
    private ProfessionalSearchPort search;

    @BeforeEach
    void cargarCatalogo() {
        replica.replaceWithSnapshot(new CatalogSnapshot(1, Instant.parse("2026-10-30T12:00:00Z"),
                List.of(new ProfessionalCategory(1L, "Clinica medica", "General", true),
                        new ProfessionalCategory(2L, "Pediatria", "Ninos", true)),
                List.of(new Professional(15L, "Ana", "Gomez", 1L, true),
                        new Professional(16L, "Luis", "Perez", 1L, false),
                        new Professional(17L, "Marta", "Ruiz", 2L, true)),
                List.of(new WeeklySchedule(21L, 15L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true),
                        new WeeklySchedule(22L, 15L, DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), LocalTime.of(18, 0), 20, true),
                        new WeeklySchedule(23L, 16L, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true),
                        new WeeklySchedule(24L, 17L, DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(13, 0), 30, false))),
                Instant.parse("2026-10-30T12:00:00Z"));
    }

    private List<Long> ids(ProfessionalSearchCriteria criteria) {
        return search.search(criteria).items().stream().map(v -> v.professional().getId()).toList();
    }

    private static ProfessionalSearchCriteria criteria(Long category, String name, Boolean enabled, DayOfWeek day, LocalTime time) {
        return new ProfessionalSearchCriteria(category, name, enabled, day, time, 0, 20);
    }

    @Test
    void sinFiltrosDevuelveTodosOrdenadosPorApellido() {
        assertThat(ids(ProfessionalSearchCriteria.all())).containsExactly(15L, 16L, 17L);
    }

    @Test
    void filtraPorCategoria() {
        assertThat(ids(criteria(1L, null, null, null, null))).containsExactly(15L, 16L);
        assertThat(ids(criteria(2L, null, null, null, null))).containsExactly(17L);
    }

    @Test
    void filtraPorNombreSinDistinguirMayusculasNiSeparandoNombreYApellido() {
        assertThat(ids(criteria(null, "ana", null, null, null))).containsExactly(15L);
        assertThat(ids(criteria(null, "GOMEZ", null, null, null))).containsExactly(15L);
        assertThat(ids(criteria(null, "ana gomez", null, null, null))).containsExactly(15L);
    }

    @Test
    void losCaracteresEspecialesDelNombreNoSeInterpretanComoComodin() {
        assertThat(ids(criteria(null, "%", null, null, null))).isEmpty();
        assertThat(ids(criteria(null, "_", null, null, null))).isEmpty();
    }

    @Test
    void filtraPorEstadoHabilitado() {
        assertThat(ids(criteria(null, null, true, null, null))).containsExactly(15L, 17L);
        assertThat(ids(criteria(null, null, false, null, null))).containsExactly(16L);
    }

    @Test
    void filtraPorDiaDeAtencionIgnorandoHorariosDeshabilitados() {
        assertThat(ids(criteria(null, null, null, DayOfWeek.MONDAY, null))).containsExactly(15L, 16L);
        assertThat(ids(criteria(null, null, null, DayOfWeek.TUESDAY, null))).isEmpty();
    }

    @Test
    void filtraPorDiaYHora() {
        assertThat(ids(criteria(null, null, null, DayOfWeek.WEDNESDAY, LocalTime.of(15, 0)))).containsExactly(15L);
        assertThat(ids(criteria(null, null, null, DayOfWeek.WEDNESDAY, LocalTime.of(18, 0)))).isEmpty();
        assertThat(ids(criteria(null, null, null, DayOfWeek.MONDAY, LocalTime.of(9, 0)))).containsExactly(15L, 16L);
    }

    @Test
    void combinaVariosFiltros() {
        assertThat(ids(criteria(1L, null, true, DayOfWeek.MONDAY, LocalTime.of(10, 0)))).containsExactly(15L);
    }

    @Test
    void paginaLosResultadosYInformaElTotal() {
        var firstPage = search.search(new ProfessionalSearchCriteria(null, null, null, null, null, 0, 2));
        var secondPage = search.search(new ProfessionalSearchCriteria(null, null, null, null, null, 1, 2));

        assertThat(firstPage.items()).hasSize(2);
        assertThat(secondPage.items()).hasSize(1);
        assertThat(firstPage.totalItems()).isEqualTo(3);
        assertThat(firstPage.totalPages()).isEqualTo(2);
    }

    @Test
    void cadaProfesionalTraeSuCategoriaYSusHorarios() {
        ProfessionalView ana = search.search(criteria(null, "ana", null, null, null)).items().get(0);

        assertThat(ana.category().getName()).isEqualTo("Clinica medica");
        assertThat(ana.schedules()).hasSize(2);
    }

    @Test
    void obtieneElDetalleDeUnProfesionalPorId() {
        assertThat(search.findViewById(15L)).isPresent();
        assertThat(search.findViewById(999L)).isEmpty();
    }
}

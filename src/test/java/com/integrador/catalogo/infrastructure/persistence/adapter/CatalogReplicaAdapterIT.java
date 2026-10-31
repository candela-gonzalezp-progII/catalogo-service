package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.AbstractPostgresIT;
import com.integrador.catalogo.application.port.out.CatalogReplicaPort;
import com.integrador.catalogo.application.port.out.ProfessionalCategoryRepositoryPort;
import com.integrador.catalogo.application.port.out.ProfessionalRepositoryPort;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.application.port.out.WeeklyScheduleRepositoryPort;
import com.integrador.catalogo.domain.model.CatalogSnapshot;
import com.integrador.catalogo.domain.model.Professional;
import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.domain.model.SyncStatus;
import com.integrador.catalogo.domain.model.WeeklySchedule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** CU-05: el snapshot se aplica completo o no se aplica nada (una sola transaccion). */
class CatalogReplicaAdapterIT extends AbstractPostgresIT {

    private static final Instant NOW = Instant.parse("2026-10-24T12:00:00Z");

    @Autowired
    private CatalogReplicaPort replica;
    @Autowired
    private SyncStatePort syncState;
    @Autowired
    private ProfessionalCategoryRepositoryPort categories;
    @Autowired
    private ProfessionalRepositoryPort professionals;
    @Autowired
    private WeeklyScheduleRepositoryPort schedules;

    @Test
    void aplicaElSnapshotCompletoYRegistraSuVersion() {
        replica.replaceWithSnapshot(snapshot(7, 1L, "Clinica medica", 15L, 1L), NOW);

        assertThat(categories.findById(1L)).isPresent();
        assertThat(professionals.findById(15L).orElseThrow().fullName()).isEqualTo("Ana Gomez");
        assertThat(schedules.findByProfessionalId(15L)).hasSize(1);
        assertThat(schedules.findByProfessionalId(15L).get(0).getSlotDurationMinutes()).isEqualTo(30);
        assertThat(syncState.get().getStatus()).isEqualTo(SyncStatus.UP_TO_DATE);
        assertThat(syncState.get().getLastSnapshotVersion()).isEqualTo(7L);
    }

    @Test
    void unSnapshotNuevoReemplazaLosDatosAnteriores() {
        replica.replaceWithSnapshot(snapshot(7, 1L, "Clinica medica", 15L, 1L), NOW);

        replica.replaceWithSnapshot(snapshot(8, 2L, "Pediatria", 16L, 2L), NOW);

        assertThat(categories.findById(1L)).isEmpty();
        assertThat(categories.findById(2L)).isPresent();
        assertThat(professionals.findById(15L)).isEmpty();
        assertThat(syncState.get().getLastSnapshotVersion()).isEqualTo(8L);
    }

    @Test
    void siElSnapshotEsInconsistenteHaceRollbackYLaVersionNoAvanza() {
        replica.replaceWithSnapshot(snapshot(7, 1L, "Clinica medica", 15L, 1L), NOW);
        // el profesional apunta a una categoria (99) que no existe: viola la clave foranea
        CatalogSnapshot roto = snapshot(8, 2L, "Pediatria", 16L, 99L);

        assertThatThrownBy(() -> replica.replaceWithSnapshot(roto, NOW)).isInstanceOf(RuntimeException.class);

        assertThat(categories.findById(1L)).isPresent();
        assertThat(categories.findById(2L)).isEmpty();
        assertThat(professionals.findById(15L)).isPresent();
        assertThat(syncState.get().getLastSnapshotVersion()).isEqualTo(7L);
    }

    private static CatalogSnapshot snapshot(long version, long categoryId, String categoryName,
                                            long professionalId, long professionalCategoryId) {
        return new CatalogSnapshot(version, NOW,
                List.of(new ProfessionalCategory(categoryId, categoryName, "desc", true)),
                List.of(new Professional(professionalId, "Ana", "Gomez", professionalCategoryId, true)),
                List.of(new WeeklySchedule(professionalId * 10, professionalId, DayOfWeek.MONDAY,
                        LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true)));
    }
}

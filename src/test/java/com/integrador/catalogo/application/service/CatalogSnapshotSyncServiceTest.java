package com.integrador.catalogo.application.service;

import com.integrador.catalogo.application.exception.CatedraUnavailableException;
import com.integrador.catalogo.application.exception.SynchronizationInProgressException;
import com.integrador.catalogo.application.port.out.CatalogReplicaPort;
import com.integrador.catalogo.application.port.out.CatedraSnapshotPort;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.domain.model.CatalogSnapshot;
import com.integrador.catalogo.domain.model.Professional;
import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.domain.model.SyncState;
import com.integrador.catalogo.domain.model.SyncStatus;
import com.integrador.catalogo.domain.model.WeeklySchedule;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogSnapshotSyncServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-24T12:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final InMemorySyncState syncState = new InMemorySyncState();
    private final CatalogReplicaPort replica = (snapshot, at) -> {
        SyncState state = syncState.get();
        state.markUpToDate(snapshot.version(), at);
        syncState.save(state);
    };

    @Test
    void aplicaElSnapshotYQuedaAlDiaConSuVersion() {
        var service = new CatalogSnapshotSyncService(() -> snapshot(7), replica, syncState, clock);

        SyncState result = service.synchronizeFromSnapshot();

        assertThat(result.getStatus()).isEqualTo(SyncStatus.UP_TO_DATE);
        assertThat(result.getLastSnapshotVersion()).isEqualTo(7L);
        assertThat(result.getLastSyncedAt()).isEqualTo(NOW);
    }

    @Test
    void siLaCatedraFallaQuedaEnErrorYConservaLaVersionAnterior() {
        new CatalogSnapshotSyncService(() -> snapshot(7), replica, syncState, clock).synchronizeFromSnapshot();
        CatedraSnapshotPort caida = () -> {
            throw new CatedraUnavailableException("catedra caida", 503, null, null);
        };
        var service = new CatalogSnapshotSyncService(caida, replica, syncState, clock);

        assertThatThrownBy(service::synchronizeFromSnapshot).isInstanceOf(CatedraUnavailableException.class);

        assertThat(syncState.current.getStatus()).isEqualTo(SyncStatus.ERROR);
        assertThat(syncState.current.getLastSnapshotVersion()).isEqualTo(7L);
        assertThat(syncState.current.getLastError()).isEqualTo("catedra caida");
    }

    @Test
    void siFallaLaAplicacionDelSnapshotLaVersionNoAvanza() {
        new CatalogSnapshotSyncService(() -> snapshot(7), replica, syncState, clock).synchronizeFromSnapshot();
        CatalogReplicaPort rota = (snapshot, at) -> {
            throw new IllegalStateException("violacion de FK");
        };
        var service = new CatalogSnapshotSyncService(() -> snapshot(8), rota, syncState, clock);

        assertThatThrownBy(service::synchronizeFromSnapshot).isInstanceOf(IllegalStateException.class);

        assertThat(syncState.current.getLastSnapshotVersion()).isEqualTo(7L);
        assertThat(syncState.current.getStatus()).isEqualTo(SyncStatus.ERROR);
    }

    @Test
    void rechazaUnaSegundaSincronizacionMientrasHayUnaEnCurso() {
        CatalogSnapshotSyncService[] holder = new CatalogSnapshotSyncService[1];
        CatedraSnapshotPort reentrante = () -> {
            assertThatThrownBy(() -> holder[0].synchronizeFromSnapshot())
                    .isInstanceOf(SynchronizationInProgressException.class);
            return snapshot(1);
        };
        holder[0] = new CatalogSnapshotSyncService(reentrante, replica, syncState, clock);

        holder[0].synchronizeFromSnapshot();

        assertThat(syncState.current.getStatus()).isEqualTo(SyncStatus.UP_TO_DATE);
    }

    private static CatalogSnapshot snapshot(long version) {
        return new CatalogSnapshot(version, NOW,
                List.of(new ProfessionalCategory(1L, "Clinica medica", "General", true)),
                List.of(new Professional(15L, "Ana", "Gomez", 1L, true)),
                List.of(new WeeklySchedule(21L, 15L, DayOfWeek.MONDAY,
                        LocalTime.of(9, 0), LocalTime.of(12, 0), 30, true)));
    }

    /** Doble de prueba: guarda copias, como lo haria una base de datos. */
    private static class InMemorySyncState implements SyncStatePort {
        SyncState current = SyncState.neverSynced();

        @Override
        public SyncState get() {
            return copy(current);
        }

        @Override
        public SyncState save(SyncState state) {
            current = copy(state);
            return copy(current);
        }

        private static SyncState copy(SyncState s) {
            return new SyncState(s.getId(), s.getLastSnapshotVersion(), s.getLastSyncedAt(),
                    s.getStatus(), s.getLastError());
        }
    }
}

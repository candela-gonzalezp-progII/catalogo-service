package com.integrador.catalogo.application.service;

import com.integrador.catalogo.application.exception.CatedraUnavailableException;
import com.integrador.catalogo.application.port.in.SynchronizeCatalogUseCase;
import com.integrador.catalogo.application.service.CatalogIncrementalSyncService;
import com.integrador.catalogo.application.service.SnapshotFallbackSyncService;
import com.integrador.catalogo.domain.model.CatalogUpdatedEvent;
import com.integrador.catalogo.domain.model.SyncOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SnapshotFallbackSyncServiceTest {

    private FakeCatalogWorld world;
    private CatalogIncrementalSyncService incremental;
    private int snapshotCalls;

    /** El "snapshot" deja la copia local en la version indicada, como lo haria CU-05. */
    private SynchronizeCatalogUseCase snapshotTo(long version) {
        return () -> {
            snapshotCalls++;
            world.localAt(version);
            return world.state;
        };
    }

    @BeforeEach
    void setUp() {
        world = new FakeCatalogWorld();
        world.oldestAvailable = 5;
        incremental = new CatalogIncrementalSyncService(world.redis, world.versionPort, world.syncStatePort,
                world.processedPort, world.clock);
    }

    private SnapshotFallbackSyncService fallback(SynchronizeCatalogUseCase snapshot) {
        return new SnapshotFallbackSyncService(incremental, snapshot, world.processedPort, world.clock);
    }

    private static CatalogUpdatedEvent event(String id, long version) {
        return new CatalogUpdatedEvent(id, version, FakeCatalogWorld.NOW, "catedra");
    }

    @Test
    void siHayDiscontinuidadReconstruyeConSnapshotYMarcaElEventoProcesado() {
        world.localAt(2);
        world.publish(5, 100);
        world.publish(6, 101);

        SyncOutcome outcome = fallback(snapshotTo(6)).handle(event("e1", 6));

        assertThat(outcome).isEqualTo(SyncOutcome.SNAPSHOT_APPLIED);
        assertThat(snapshotCalls).isEqualTo(1);
        assertThat(world.state.getLastSnapshotVersion()).isEqualTo(6L);
        assertThat(world.processed).contains("e1");
    }

    @Test
    void despuesDelSnapshotSePoneAlDiaSiHabiaAlgoMasNuevo() {
        world.localAt(2);
        world.publish(5, 100);
        world.publish(6, 101);
        world.publish(7, 102);

        fallback(snapshotTo(6)).handle(event("e2", 7));

        assertThat(world.appliedVersions).isEqualTo(java.util.List.of(7L));
        assertThat(world.state.getLastSnapshotVersion()).isEqualTo(7L);
    }

    @Test
    void sinoHayProblemasNoUsaElSnapshot() {
        world.localAt(5);
        world.publish(6, 100);

        SyncOutcome outcome = fallback(snapshotTo(6)).handle(event("e3", 6));

        assertThat(outcome).isEqualTo(SyncOutcome.INCREMENTAL_APPLIED);
        assertThat(snapshotCalls).isEqualTo(0);
    }

    @Test
    void siElSnapshotFallaElErrorSubeYElEventoNoSeMarcaProcesado() {
        world.localAt(2);
        world.publish(5, 100);
        SynchronizeCatalogUseCase caido = () -> {
            throw new CatedraUnavailableException("catedra caida", 503, null, null);
        };

        assertThatThrownBy(() -> fallback(caido).handle(event("e4", 5)))
                .isInstanceOf(CatedraUnavailableException.class);

        assertThat(world.processed).doesNotContain("e4");
    }

    @Test
    void catchUpTambienUsaElFallback() {
        world.localAt(1);
        world.publish(5, 100);

        SyncOutcome outcome = fallback(snapshotTo(5)).catchUp();

        assertThat(outcome).isEqualTo(SyncOutcome.SNAPSHOT_APPLIED);
    }
}

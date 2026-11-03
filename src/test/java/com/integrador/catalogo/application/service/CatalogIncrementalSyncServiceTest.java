package com.integrador.catalogo.application.service;

import com.integrador.catalogo.application.exception.CatalogDiscontinuityException;
import com.integrador.catalogo.domain.model.CatalogUpdatedEvent;
import com.integrador.catalogo.domain.model.SyncOutcome;
import com.integrador.catalogo.domain.model.SyncStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CatalogIncrementalSyncServiceTest {

    private FakeCatalogWorld world;
    private CatalogIncrementalSyncService service;

    @BeforeEach
    void setUp() {
        world = new FakeCatalogWorld();
        world.oldestAvailable = 1;
        service = new CatalogIncrementalSyncService(world.redis, world.versionPort, world.syncStatePort,
                world.processedPort, world.clock);
    }

    private static CatalogUpdatedEvent event(String id, long version) {
        return new CatalogUpdatedEvent(id, version, FakeCatalogWorld.NOW, "catedra");
    }

    @Test
    void aplicaLasVersionesFaltantesEnOrden() {
        world.localAt(5);
        world.publish(6, 100);
        world.publish(7, 101);
        world.publish(8, 102);

        SyncOutcome outcome = service.handle(event("e1", 8));

        assertThat(outcome).isEqualTo(SyncOutcome.INCREMENTAL_APPLIED);
        assertThat(world.appliedVersions).isEqualTo(List.of(6L, 7L, 8L));
        assertThat(world.state.getLastSnapshotVersion()).isEqualTo(8L);
    }

    @Test
    void ignoraUnEventoYaProcesado() {
        world.localAt(5);
        world.publish(6, 100);
        service.handle(event("e1", 6));

        SyncOutcome again = service.handle(event("e1", 6));

        assertThat(again).isEqualTo(SyncOutcome.IGNORED_DUPLICATE);
        assertThat(world.appliedVersions).isEqualTo(List.of(6L));
    }

    @Test
    void unEventoViejoNoHaceRetrocederLaVersion() {
        world.localAt(8);
        world.publish(8, 100);

        SyncOutcome outcome = service.handle(event("e2", 6));

        assertThat(outcome).isEqualTo(SyncOutcome.ALREADY_UP_TO_DATE);
        assertThat(world.appliedVersions).isEmpty();
        assertThat(world.state.getLastSnapshotVersion()).isEqualTo(8L);
    }

    @Test
    void siLaVersionLocalEsAnteriorALaMasAntiguaDisponibleHayDiscontinuidad() {
        world.localAt(2);
        world.oldestAvailable = 5;
        world.publish(5, 100);
        world.publish(6, 101);

        assertThatThrownBy(() -> service.handle(event("e3", 6)))
                .isInstanceOf(CatalogDiscontinuityException.class);

        assertThat(world.appliedVersions).isEmpty();
        assertThat(world.processed).doesNotContain("e3");
    }

    @Test
    void sinVersionLocalPideUnSnapshot() {
        world.publish(1, 100);

        assertThatThrownBy(() -> service.handle(event("e4", 1)))
                .isInstanceOf(CatalogDiscontinuityException.class);
    }

    @Test
    void siFaltaElRegistroDeCambiosDeUnaVersionHayDiscontinuidadYLaVersionNoAvanza() {
        world.localAt(5);
        world.publish(7, 100);
        world.currentVersion = 7;

        assertThatThrownBy(() -> service.handle(event("e5", 7)))
                .isInstanceOf(CatalogDiscontinuityException.class);

        assertThat(world.state.getLastSnapshotVersion()).isEqualTo(5L);
    }

    @Test
    void siRedisNoTieneUnaEntidadReferenciadaHayDiscontinuidad() {
        world.localAt(5);
        world.publish(6, 100);
        world.professionals.clear();

        assertThatThrownBy(() -> service.handle(event("e6", 6)))
                .isInstanceOf(CatalogDiscontinuityException.class);
    }

    @Test
    void siFallaLaBaseLaVersionNoAvanzaYQuedaEnError() {
        world.localAt(5);
        world.publish(6, 100);
        world.failOnApply = true;

        assertThatThrownBy(() -> service.handle(event("e7", 6))).isInstanceOf(IllegalStateException.class);

        assertThat(world.state.getLastSnapshotVersion()).isEqualTo(5L);
        assertThat(world.state.getStatus()).isEqualTo(SyncStatus.ERROR);
        assertThat(world.processed).doesNotContain("e7");
    }

    @Test
    void catchUpTomaLaVersionActualDeRedis() {
        world.localAt(5);
        world.publish(6, 100);
        world.publish(7, 101);

        assertThat(service.catchUp()).isEqualTo(SyncOutcome.INCREMENTAL_APPLIED);
        assertThat(world.state.getLastSnapshotVersion()).isEqualTo(7L);
        assertThat(service.catchUp()).isEqualTo(SyncOutcome.ALREADY_UP_TO_DATE);
    }
}

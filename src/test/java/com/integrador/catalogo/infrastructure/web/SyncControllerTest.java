package com.integrador.catalogo.infrastructure.web;

import com.integrador.catalogo.domain.model.SyncState;
import com.integrador.catalogo.domain.model.SyncStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/** CU-17: el endpoint informa version, estado y ultimo error. */
class SyncControllerTest {

    @Test
    void informaElEstadoAlDiaConSuVersion() {
        SyncState state = SyncState.neverSynced();
        state.markUpToDate(7L, Instant.parse("2026-10-24T12:00:00Z"));
        SyncController controller = new SyncController(() -> state);

        SyncStatusResponse response = controller.status();

        assertThat(response.status()).isEqualTo(SyncStatus.UP_TO_DATE);
        assertThat(response.lastSnapshotVersion()).isEqualTo(7L);
        assertThat(response.lastError()).isNull();
    }

    @Test
    void informaElUltimoErrorSinDatosInternos() {
        SyncState state = SyncState.neverSynced();
        state.markError("La catedra respondio HTTP 503");
        SyncController controller = new SyncController(() -> state);

        SyncStatusResponse response = controller.status();

        assertThat(response.status()).isEqualTo(SyncStatus.ERROR);
        assertThat(response.lastError()).isEqualTo("La catedra respondio HTTP 503");
    }

    @Test
    void informaQueNuncaSeSincronizo() {
        SyncController controller = new SyncController(SyncState::neverSynced);

        assertThat(controller.status().status()).isEqualTo(SyncStatus.NEVER_SYNCED);
    }
}

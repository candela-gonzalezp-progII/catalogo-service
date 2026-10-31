package com.integrador.catalogo.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class SyncStateTest {

    @Test
    void arrancaSinSincronizar() {
        SyncState state = SyncState.neverSynced();

        assertThat(state.getStatus()).isEqualTo(SyncStatus.NEVER_SYNCED);
        assertThat(state.getLastSnapshotVersion()).isNull();
    }

    @Test
    void elErrorNoModificaLaUltimaVersionAplicada() {
        SyncState state = SyncState.neverSynced();
        state.markUpToDate(7L, Instant.parse("2026-10-24T12:00:00Z"));

        state.markError("catedra caida");

        assertThat(state.getStatus()).isEqualTo(SyncStatus.ERROR);
        assertThat(state.getLastSnapshotVersion()).isEqualTo(7L);
        assertThat(state.getLastError()).isEqualTo("catedra caida");
    }

    @Test
    void sincronizarConExitoLimpiaElErrorAnterior() {
        SyncState state = SyncState.neverSynced();
        state.markError("fallo");

        state.markUpToDate(8L, Instant.parse("2026-10-24T12:00:00Z"));

        assertThat(state.getStatus()).isEqualTo(SyncStatus.UP_TO_DATE);
        assertThat(state.getLastError()).isNull();
    }

    @Test
    void recortaMensajesDeErrorMuyLargos() {
        SyncState state = SyncState.neverSynced();

        state.markError("x".repeat(2000));

        assertThat(state.getLastError()).hasSize(SyncState.MAX_ERROR_LENGTH);
    }
}

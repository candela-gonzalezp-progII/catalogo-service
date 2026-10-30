package com.integrador.catalogo.infrastructure.web;

import com.integrador.catalogo.domain.model.SyncState;
import com.integrador.catalogo.domain.model.SyncStatus;

import java.time.Instant;

/** Respuesta publica de GET /api/sync/status. No incluye secretos ni trazas internas. */
public record SyncStatusResponse(SyncStatus status, Long lastSnapshotVersion, Instant lastSyncedAt, String lastError) {

    public static SyncStatusResponse from(SyncState state) {
        return new SyncStatusResponse(state.getStatus(), state.getLastSnapshotVersion(),
                state.getLastSyncedAt(), state.getLastError());
    }
}

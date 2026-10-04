package com.integrador.catalogo.domain.model;

import java.time.Instant;

/**
 * Estado de sincronizacion del catalogo local respecto del servicio central.
 * Existe una unica instancia logica (fila con id fijo) por servicio.
 */
public class SyncState {

    public static final Long SINGLETON_ID = 1L;

    private final Long id;
    private Long lastSnapshotVersion;
    private Instant lastSyncedAt;
    private SyncStatus status;

    public SyncState(Long id, Long lastSnapshotVersion, Instant lastSyncedAt, SyncStatus status) {
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser null");
        }
        if (status == null) {
            throw new IllegalArgumentException("status no puede ser null");
        }
        this.id = id;
        this.lastSnapshotVersion = lastSnapshotVersion;
        this.lastSyncedAt = lastSyncedAt;
        this.status = status;
    }

    public static SyncState neverSynced() {
        return new SyncState(SINGLETON_ID, null, null, SyncStatus.NEVER_SYNCED);
    }

    public Long getId() {
        return id;
    }

    public Long getLastSnapshotVersion() {
        return lastSnapshotVersion;
    }

    public Instant getLastSyncedAt() {
        return lastSyncedAt;
    }

    public SyncStatus getStatus() {
        return status;
    }

    public void markUpToDate(Long snapshotVersion, Instant syncedAt) {
        this.lastSnapshotVersion = snapshotVersion;
        this.lastSyncedAt = syncedAt;
        this.status = SyncStatus.UP_TO_DATE;
    }

    public void markError() {
        this.status = SyncStatus.ERROR;
    }
}

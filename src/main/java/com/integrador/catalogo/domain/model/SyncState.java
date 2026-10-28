package com.integrador.catalogo.domain.model;

import java.time.Instant;

/**
 * Estado de sincronizacion del catalogo local respecto del servicio central.
 * Existe una unica instancia logica (fila con id fijo) por servicio.
 */
public class SyncState {

    public static final Long SINGLETON_ID = 1L;
    public static final int MAX_ERROR_LENGTH = 500;

    private final Long id;
    private Long lastSnapshotVersion;
    private Instant lastSyncedAt;
    private SyncStatus status;
    private String lastError;

    public SyncState(Long id, Long lastSnapshotVersion, Instant lastSyncedAt,
                     SyncStatus status, String lastError) {
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
        this.lastError = lastError;
    }

    public static SyncState neverSynced() {
        return new SyncState(SINGLETON_ID, null, null, SyncStatus.NEVER_SYNCED, null);
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

    public String getLastError() {
        return lastError;
    }

    public void markSyncing() {
        this.status = SyncStatus.SYNCING;
    }

    /** La version solo se registra cuando los datos de esa version ya quedaron aplicados. */
    public void markUpToDate(Long snapshotVersion, Instant syncedAt) {
        this.lastSnapshotVersion = snapshotVersion;
        this.lastSyncedAt = syncedAt;
        this.status = SyncStatus.UP_TO_DATE;
        this.lastError = null;
    }

    /** Marca el error sin tocar la ultima version aplicada (los datos locales siguen siendo los de esa version). */
    public void markError(String message) {
        this.status = SyncStatus.ERROR;
        this.lastError = truncate(message);
    }

    private static String truncate(String message) {
        if (message == null || message.isBlank()) {
            return "error de sincronizacion sin detalle";
        }
        return message.length() <= MAX_ERROR_LENGTH ? message : message.substring(0, MAX_ERROR_LENGTH);
    }
}

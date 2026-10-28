package com.integrador.catalogo.application.service;

import com.integrador.catalogo.application.exception.SynchronizationInProgressException;
import com.integrador.catalogo.application.port.in.SynchronizeCatalogUseCase;
import com.integrador.catalogo.application.port.out.CatalogReplicaPort;
import com.integrador.catalogo.application.port.out.CatedraSnapshotPort;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.domain.model.CatalogSnapshot;
import com.integrador.catalogo.domain.model.SyncState;

import java.time.Clock;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CU-05. Orquesta: marcar SYNCING -> pedir snapshot -> reemplazar la replica de forma atomica.
 * Si algo falla, la replica no cambia (rollback) y el estado queda en ERROR con el motivo.
 */
public class CatalogSnapshotSyncService implements SynchronizeCatalogUseCase {

    private final CatedraSnapshotPort snapshotPort;
    private final CatalogReplicaPort replicaPort;
    private final SyncStatePort syncStatePort;
    private final Clock clock;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CatalogSnapshotSyncService(CatedraSnapshotPort snapshotPort, CatalogReplicaPort replicaPort,
                                      SyncStatePort syncStatePort, Clock clock) {
        this.snapshotPort = snapshotPort;
        this.replicaPort = replicaPort;
        this.syncStatePort = syncStatePort;
        this.clock = clock;
    }

    @Override
    public SyncState synchronizeFromSnapshot() {
        if (!running.compareAndSet(false, true)) {
            throw new SynchronizationInProgressException();
        }
        try {
            return runSynchronization();
        } finally {
            running.set(false);
        }
    }

    private SyncState runSynchronization() {
        markSyncing();
        try {
            CatalogSnapshot snapshot = snapshotPort.fetchSnapshot();
            replicaPort.replaceWithSnapshot(snapshot, clock.instant());
            return syncStatePort.get();
        } catch (RuntimeException e) {
            markError(e);
            throw e;
        }
    }

    private void markSyncing() {
        SyncState state = syncStatePort.get();
        state.markSyncing();
        syncStatePort.save(state);
    }

    private void markError(RuntimeException cause) {
        SyncState state = syncStatePort.get();
        state.markError(cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
        syncStatePort.save(state);
    }
}

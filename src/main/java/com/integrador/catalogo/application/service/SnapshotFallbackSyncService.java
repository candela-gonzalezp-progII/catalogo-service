package com.integrador.catalogo.application.service;

import com.integrador.catalogo.application.exception.CatalogDiscontinuityException;
import com.integrador.catalogo.application.port.in.HandleCatalogUpdateUseCase;
import com.integrador.catalogo.application.port.in.SynchronizeCatalogUseCase;
import com.integrador.catalogo.application.port.out.ProcessedEventPort;
import com.integrador.catalogo.domain.model.CatalogUpdatedEvent;
import com.integrador.catalogo.domain.model.SyncOutcome;

import java.time.Clock;

/**
 * CU-07. Decorator del caso de uso incremental: si no se puede continuar de forma segura
 * (CatalogDiscontinuityException), reconstruye la copia local con un snapshot completo (CU-05)
 * y vuelve a ponerse al dia UNA sola vez. No hay bucles: si falla de nuevo, el error sube y
 * queda registrado en el estado de sincronizacion.
 */
public class SnapshotFallbackSyncService implements HandleCatalogUpdateUseCase {

    private final HandleCatalogUpdateUseCase incremental;
    private final SynchronizeCatalogUseCase snapshot;
    private final ProcessedEventPort processedEvents;
    private final Clock clock;

    public SnapshotFallbackSyncService(HandleCatalogUpdateUseCase incremental, SynchronizeCatalogUseCase snapshot,
                                       ProcessedEventPort processedEvents, Clock clock) {
        this.incremental = incremental;
        this.snapshot = snapshot;
        this.processedEvents = processedEvents;
        this.clock = clock;
    }

    @Override
    public synchronized SyncOutcome handle(CatalogUpdatedEvent event) {
        try {
            return incremental.handle(event);
        } catch (CatalogDiscontinuityException discontinuity) {
            rebuildFromSnapshot();
            processedEvents.markProcessed(event.eventId(), clock.instant());
            return SyncOutcome.SNAPSHOT_APPLIED;
        }
    }

    @Override
    public synchronized SyncOutcome catchUp() {
        try {
            return incremental.catchUp();
        } catch (CatalogDiscontinuityException discontinuity) {
            rebuildFromSnapshot();
            return SyncOutcome.SNAPSHOT_APPLIED;
        }
    }

    private void rebuildFromSnapshot() {
        snapshot.synchronizeFromSnapshot();
        // El snapshot puede ser anterior a lo ultimo publicado: un unico intento mas de ponerse al dia.
        incremental.catchUp();
    }
}

package com.integrador.catalogo.infrastructure.config;

import com.integrador.catalogo.application.port.in.SynchronizeCatalogUseCase;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.domain.model.SyncStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * CU-05: al arrancar con la base local vacia (o con error previo) pide el snapshot completo.
 * Se activa con CATEDRA_SYNC_ON_STARTUP=true. Si la catedra no responde, la app igual arranca
 * y el error queda visible en GET /api/sync/status.
 */
@Component
@ConditionalOnProperty(name = "catedra.sync-on-startup", havingValue = "true")
public class StartupSnapshotRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(StartupSnapshotRunner.class);

    private final SynchronizeCatalogUseCase synchronize;
    private final SyncStatePort syncState;

    public StartupSnapshotRunner(SynchronizeCatalogUseCase synchronize, SyncStatePort syncState) {
        this.synchronize = synchronize;
        this.syncState = syncState;
    }

    @Override
    public void run(ApplicationArguments args) {
        var current = syncState.get();
        if (current.getStatus() == SyncStatus.UP_TO_DATE) {
            log.info("Catalogo ya sincronizado (version {}), no se pide snapshot", current.getLastSnapshotVersion());
            return;
        }
        try {
            var result = synchronize.synchronizeFromSnapshot();
            log.info("Snapshot aplicado: version {}", result.getLastSnapshotVersion());
        } catch (RuntimeException e) {
            log.warn("No se pudo aplicar el snapshot inicial: {}", e.getMessage());
        }
    }
}

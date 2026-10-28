package com.integrador.catalogo.infrastructure.config;

import com.integrador.catalogo.application.port.in.SynchronizeCatalogUseCase;
import com.integrador.catalogo.application.port.out.CatalogReplicaPort;
import com.integrador.catalogo.application.port.out.CatedraSnapshotPort;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.application.service.CatalogSnapshotSyncService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Cableado de la capa de aplicacion. Los casos de uso son Java puro (sin anotaciones de Spring):
 * es esta clase de infraestructura la que los registra como beans.
 */
@Configuration
public class ApplicationConfig {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    SynchronizeCatalogUseCase synchronizeCatalogUseCase(CatedraSnapshotPort snapshotPort,
                                                        CatalogReplicaPort replicaPort,
                                                        SyncStatePort syncStatePort, Clock clock) {
        return new CatalogSnapshotSyncService(snapshotPort, replicaPort, syncStatePort, clock);
    }
}

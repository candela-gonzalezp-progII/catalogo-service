package com.integrador.catalogo.infrastructure.config;

import com.integrador.catalogo.application.port.in.HandleCatalogUpdateUseCase;
import com.integrador.catalogo.application.port.in.SynchronizeCatalogUseCase;
import com.integrador.catalogo.application.port.out.CatalogVersionPort;
import com.integrador.catalogo.application.port.out.CatedraRedisPort;
import com.integrador.catalogo.application.port.out.ProcessedEventPort;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.application.service.CatalogIncrementalSyncService;
import com.integrador.catalogo.application.service.SnapshotFallbackSyncService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * CU-06 + CU-07: el caso de uso incremental queda envuelto por el fallback a snapshot (Decorator).
 * Si hay discontinuidad, se reconstruye la copia local con el snapshot completo (CU-05).
 */
@Configuration
public class IncrementalSyncConfig {

    @Bean
    HandleCatalogUpdateUseCase handleCatalogUpdateUseCase(CatedraRedisPort redis, CatalogVersionPort versionPort,
                                                          SyncStatePort syncStatePort, ProcessedEventPort processedEvents,
                                                          SynchronizeCatalogUseCase snapshot, Clock clock) {
        var incremental = new CatalogIncrementalSyncService(redis, versionPort, syncStatePort, processedEvents, clock);
        return new SnapshotFallbackSyncService(incremental, snapshot, processedEvents, clock);
    }
}

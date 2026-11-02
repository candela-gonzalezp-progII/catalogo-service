package com.integrador.catalogo.infrastructure.config;

import com.integrador.catalogo.application.port.in.HandleCatalogUpdateUseCase;
import com.integrador.catalogo.application.port.out.CatalogVersionPort;
import com.integrador.catalogo.application.port.out.CatedraRedisPort;
import com.integrador.catalogo.application.port.out.ProcessedEventPort;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.application.service.CatalogIncrementalSyncService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** CU-06: arma el caso de uso incremental. (CU-07 lo envuelve con el fallback a snapshot.) */
@Configuration
public class IncrementalSyncConfig {

    @Bean
    HandleCatalogUpdateUseCase handleCatalogUpdateUseCase(CatedraRedisPort redis, CatalogVersionPort versionPort,
                                                          SyncStatePort syncStatePort, ProcessedEventPort processedEvents,
                                                          Clock clock) {
        return new CatalogIncrementalSyncService(redis, versionPort, syncStatePort, processedEvents, clock);
    }
}

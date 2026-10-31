package com.integrador.catalogo.infrastructure.config;

import com.integrador.catalogo.application.port.in.GetSyncStatusUseCase;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.application.service.SyncStatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SyncStatusConfig {

    @Bean
    GetSyncStatusUseCase getSyncStatusUseCase(SyncStatePort syncStatePort) {
        return new SyncStatusService(syncStatePort);
    }
}

package com.integrador.catalogo.infrastructure.web;

import com.integrador.catalogo.application.port.in.GetSyncStatusUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Adaptador de entrada de CU-17. */
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final GetSyncStatusUseCase getSyncStatus;

    public SyncController(GetSyncStatusUseCase getSyncStatus) {
        this.getSyncStatus = getSyncStatus;
    }

    @GetMapping("/status")
    public SyncStatusResponse status() {
        return SyncStatusResponse.from(getSyncStatus.getStatus());
    }
}

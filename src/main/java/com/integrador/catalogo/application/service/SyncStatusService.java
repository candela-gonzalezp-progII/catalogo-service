package com.integrador.catalogo.application.service;

import com.integrador.catalogo.application.port.in.GetSyncStatusUseCase;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.domain.model.SyncState;

/** CU-17. */
public class SyncStatusService implements GetSyncStatusUseCase {

    private final SyncStatePort syncStatePort;

    public SyncStatusService(SyncStatePort syncStatePort) {
        this.syncStatePort = syncStatePort;
    }

    @Override
    public SyncState getStatus() {
        return syncStatePort.get();
    }
}

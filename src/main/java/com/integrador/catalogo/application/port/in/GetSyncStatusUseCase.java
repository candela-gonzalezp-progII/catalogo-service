package com.integrador.catalogo.application.port.in;

import com.integrador.catalogo.domain.model.SyncState;

/** CU-17: informar el estado y el ultimo error de sincronizacion. */
public interface GetSyncStatusUseCase {

    SyncState getStatus();
}

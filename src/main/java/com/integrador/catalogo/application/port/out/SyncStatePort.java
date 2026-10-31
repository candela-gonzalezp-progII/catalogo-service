package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.SyncState;

public interface SyncStatePort {

    SyncState get();

    SyncState save(SyncState state);
}

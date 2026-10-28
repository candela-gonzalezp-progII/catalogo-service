package com.integrador.catalogo.infrastructure.catedra;

import com.integrador.catalogo.application.port.out.CatedraSnapshotPort;
import com.integrador.catalogo.domain.model.CatalogSnapshot;
import com.integrador.catalogo.infrastructure.catedra.dto.SnapshotResponse;

/** Adaptador de salida de CU-05: GET /api/synchronization/snapshot. */
public class CatedraSnapshotAdapter implements CatedraSnapshotPort {

    private final CatedraApiClient api;

    public CatedraSnapshotAdapter(CatedraApiClient api) {
        this.api = api;
    }

    @Override
    public CatalogSnapshot fetchSnapshot() {
        return api.get("/api/synchronization/snapshot", SnapshotResponse.class).toDomain();
    }
}

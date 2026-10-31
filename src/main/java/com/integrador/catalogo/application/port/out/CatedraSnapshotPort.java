package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.CatalogSnapshot;

public interface CatedraSnapshotPort {

    /** GET /api/synchronization/snapshot */
    CatalogSnapshot fetchSnapshot();
}

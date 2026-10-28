package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.CatalogSnapshot;

import java.time.Instant;

public interface CatalogReplicaPort {

    /**
     * Reemplaza TODO el catalogo local por el snapshot y registra su version como version local,
     * en una unica unidad de trabajo: o queda todo aplicado, o no cambia nada.
     */
    void replaceWithSnapshot(CatalogSnapshot snapshot, Instant syncedAt);
}

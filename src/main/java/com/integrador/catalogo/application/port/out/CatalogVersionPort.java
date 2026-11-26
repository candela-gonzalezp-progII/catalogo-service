package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.VersionData;

import java.time.Instant;

public interface CatalogVersionPort {

    /**
     * Aplica UNA version incremental en una sola transaccion (upsert de categorias, profesionales y
     * horarios) y recien al final registra esa version como version local.
     */
    void applyVersion(VersionData data, Instant syncedAt);
}

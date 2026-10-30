package com.integrador.catalogo.domain.model;

import java.time.Instant;

/** Versiones publicadas por la catedra en Redis (ventana de cambios disponibles). */
public record SyncMetadata(long currentVersion, long oldestAvailableVersion, Instant publishedAt) {

    public SyncMetadata {
        if (oldestAvailableVersion > currentVersion) {
            throw new IllegalArgumentException("oldestAvailableVersion no puede superar a currentVersion");
        }
    }

    /** Regla del anexo: si la version local es menor a la mas antigua disponible, hace falta un snapshot. */
    public boolean requiresSnapshot(long localVersion) {
        return localVersion < oldestAvailableVersion;
    }

    public boolean isBehind(long localVersion) {
        return localVersion < currentVersion;
    }
}

package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.SyncMetadata;
import com.integrador.catalogo.domain.model.VersionChanges;

import java.util.Optional;

/** Lectura (solo lectura) del catalogo publicado por la catedra en Redis. */
public interface CatedraRedisPort {

    SyncMetadata readMetadata();

    /** Vacio si esa version ya no esta en la ventana retenida. */
    Optional<VersionChanges> readChanges(long version);
}

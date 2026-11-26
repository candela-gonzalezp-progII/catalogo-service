package com.integrador.catalogo.application.port.out;

import com.integrador.catalogo.domain.model.Professional;
import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.domain.model.SyncMetadata;
import com.integrador.catalogo.domain.model.VersionChanges;
import com.integrador.catalogo.domain.model.WeeklySchedule;

import java.util.Optional;

/** Lectura (solo lectura) del catalogo publicado por la catedra en Redis. */
public interface CatedraRedisPort {

    SyncMetadata readMetadata();

    /** Vacio si esa version ya no esta en la ventana retenida. */
    Optional<VersionChanges> readChanges(long version);

    /** Estado vigente de una entidad (las deshabilitadas siguen presentes con enabled=false). */
    Optional<ProfessionalCategory> readCategory(long id);

    Optional<Professional> readProfessional(long id);

    Optional<WeeklySchedule> readSchedule(long id);
}

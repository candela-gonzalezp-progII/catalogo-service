package com.integrador.catalogo.application.port.in;

import com.integrador.catalogo.domain.model.CatalogUpdatedEvent;
import com.integrador.catalogo.domain.model.SyncOutcome;

/** CU-06: actualizar la copia local a partir de una notificacion de nueva version. */
public interface HandleCatalogUpdateUseCase {

    /** Procesa un CatalogUpdated. Es idempotente: repetirlo no cambia el resultado final. */
    SyncOutcome handle(CatalogUpdatedEvent event);

    /** Se pone al dia con la version actual publicada en Redis (por ejemplo al arrancar, si se perdieron notificaciones). */
    SyncOutcome catchUp();
}

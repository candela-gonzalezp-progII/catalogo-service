    package com.integrador.catalogo.application.port.in;

    import com.integrador.catalogo.domain.model.SyncState;

    /** CU-05: inicializar o reconstruir el catalogo local desde un snapshot completo. */
    public interface SynchronizeCatalogUseCase {

        /** @return el estado de sincronizacion resultante (UP_TO_DATE si salio bien). */
        SyncState synchronizeFromSnapshot();
    }

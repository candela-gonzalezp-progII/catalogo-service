package com.integrador.catalogo.application.service;

import com.integrador.catalogo.application.exception.CatalogDiscontinuityException;
import com.integrador.catalogo.application.port.in.HandleCatalogUpdateUseCase;
import com.integrador.catalogo.application.port.out.CatalogVersionPort;
import com.integrador.catalogo.application.port.out.CatedraRedisPort;
import com.integrador.catalogo.application.port.out.ProcessedEventPort;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.domain.model.CatalogUpdatedEvent;
import com.integrador.catalogo.domain.model.SyncMetadata;
import com.integrador.catalogo.domain.model.SyncOutcome;
import com.integrador.catalogo.domain.model.SyncState;
import com.integrador.catalogo.domain.model.VersionChanges;
import com.integrador.catalogo.domain.model.VersionData;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * CU-06. Aplica, EN ORDEN y de a una, las versiones posteriores a la local:
 * lee los IDs de changes:{version} y el estado vigente de cada ID desde los Hashes de Redis.
 *
 * Reglas del anexo que cumple:
 *  - la version local solo avanza cuando la unidad de trabajo (una version) termino bien;
 *  - procesar dos veces el mismo evento o la misma version no cambia el resultado;
 *  - si no se puede continuar de forma segura lanza CatalogDiscontinuityException (CU-07 la resuelve).
 */
public class CatalogIncrementalSyncService implements HandleCatalogUpdateUseCase {

    private final CatedraRedisPort redis;
    private final CatalogVersionPort versionPort;
    private final SyncStatePort syncStatePort;
    private final ProcessedEventPort processedEvents;
    private final Clock clock;

    public CatalogIncrementalSyncService(CatedraRedisPort redis, CatalogVersionPort versionPort,
                                         SyncStatePort syncStatePort, ProcessedEventPort processedEvents, Clock clock) {
        this.redis = redis;
        this.versionPort = versionPort;
        this.syncStatePort = syncStatePort;
        this.processedEvents = processedEvents;
        this.clock = clock;
    }

    @Override
    public synchronized SyncOutcome handle(CatalogUpdatedEvent event) {
        if (processedEvents.wasProcessed(event.eventId())) {
            return SyncOutcome.IGNORED_DUPLICATE;
        }
        SyncOutcome outcome = syncUpTo(event.newVersion());
        processedEvents.markProcessed(event.eventId(), clock.instant());
        return outcome;
    }

    @Override
    public synchronized SyncOutcome catchUp() {
        return syncUpTo(redis.readMetadata().currentVersion());
    }

    private SyncOutcome syncUpTo(long targetVersion) {
        Long local = syncStatePort.get().getLastSnapshotVersion();
        if (local == null) {
            throw new CatalogDiscontinuityException("No hay version local: hace falta un snapshot completo");
        }
        if (targetVersion <= local) {
            return SyncOutcome.ALREADY_UP_TO_DATE;
        }
        SyncMetadata metadata = redis.readMetadata();
        if (metadata.requiresSnapshot(local)) {
            throw new CatalogDiscontinuityException("La version local " + local
                    + " es anterior a la mas antigua disponible (" + metadata.oldestAvailableVersion() + ")");
        }
        long goal = Math.min(Math.max(targetVersion, local), metadata.currentVersion());
        if (goal <= local) {
            return SyncOutcome.ALREADY_UP_TO_DATE;
        }
        for (long version = local + 1; version <= goal; version++) {
            applyOneVersion(version);
        }
        return SyncOutcome.INCREMENTAL_APPLIED;
    }

    private void applyOneVersion(long version) {
        VersionChanges changes = redis.readChanges(version).orElseThrow(() ->
                new CatalogDiscontinuityException("Falta el registro de cambios de la version " + version));
        VersionData data = new VersionData(version,
                load(changes.categoryIds(), "categoria", redis::readCategory),
                load(changes.professionalIds(), "profesional", redis::readProfessional),
                load(changes.scheduleIds(), "horario", redis::readSchedule));
        try {
            versionPort.applyVersion(data, clock.instant());
        } catch (RuntimeException e) {
            markError(e);
            throw e;
        }
    }

    private static <T> List<T> load(List<Long> ids, String kind, Function<Long, Optional<T>> reader) {
        return ids.stream()
                .map(id -> reader.apply(id).orElseThrow(() ->
                        new CatalogDiscontinuityException("Redis no tiene la " + kind + " " + id)))
                .toList();
    }

    private void markError(RuntimeException cause) {
        SyncState state = syncStatePort.get();
        state.markError(cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
        syncStatePort.save(state);
    }
}

package com.integrador.catalogo.application.service;

import com.integrador.catalogo.application.port.out.CatalogVersionPort;
import com.integrador.catalogo.application.port.out.CatedraRedisPort;
import com.integrador.catalogo.application.port.out.ProcessedEventPort;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.domain.model.Professional;
import com.integrador.catalogo.domain.model.ProfessionalCategory;
import com.integrador.catalogo.domain.model.SyncMetadata;
import com.integrador.catalogo.domain.model.SyncState;
import com.integrador.catalogo.domain.model.VersionChanges;
import com.integrador.catalogo.domain.model.VersionData;
import com.integrador.catalogo.domain.model.WeeklySchedule;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Dobles de prueba en memoria: una "catedra" (Redis) y una "base local" para los tests de CU-06 y CU-07. */
class FakeCatalogWorld {

    static final Instant NOW = Instant.parse("2026-11-01T12:00:00Z");
    final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    // --- lado catedra (Redis)
    long currentVersion;
    long oldestAvailable;
    final Map<Long, VersionChanges> changes = new HashMap<>();
    final Map<Long, Professional> professionals = new HashMap<>();
    final Map<Long, ProfessionalCategory> categories = new HashMap<>();
    final Map<Long, WeeklySchedule> schedules = new HashMap<>();

    // --- lado local
    SyncState state = SyncState.neverSynced();
    final List<Long> appliedVersions = new ArrayList<>();
    final Set<String> processed = new HashSet<>();
    boolean failOnApply;

    final CatedraRedisPort redis = new CatedraRedisPort() {
        public SyncMetadata readMetadata() {
            return new SyncMetadata(currentVersion, oldestAvailable, NOW);
        }

        public Optional<VersionChanges> readChanges(long version) {
            return Optional.ofNullable(changes.get(version));
        }

        public Optional<ProfessionalCategory> readCategory(long id) {
            return Optional.ofNullable(categories.get(id));
        }

        public Optional<Professional> readProfessional(long id) {
            return Optional.ofNullable(professionals.get(id));
        }

        public Optional<WeeklySchedule> readSchedule(long id) {
            return Optional.ofNullable(schedules.get(id));
        }
    };

    final CatalogVersionPort versionPort = (data, at) -> {
        if (failOnApply) {
            throw new IllegalStateException("fallo de base de datos");
        }
        appliedVersions.add(data.version());
        SyncState s = copy(state);
        s.markUpToDate(data.version(), at);
        state = s;
    };

    final SyncStatePort syncStatePort = new SyncStatePort() {
        public SyncState get() {
            return copy(state);
        }

        public SyncState save(SyncState s) {
            state = copy(s);
            return copy(state);
        }
    };

    final ProcessedEventPort processedPort = new ProcessedEventPort() {
        public boolean wasProcessed(String eventId) {
            return processed.contains(eventId);
        }

        public void markProcessed(String eventId, Instant at) {
            processed.add(eventId);
        }
    };

    /** Publica una version en la "catedra": un profesional modificado por version. */
    void publish(long version, long professionalId) {
        professionals.put(professionalId, new Professional(professionalId, "Prof" + version, "Apellido", 1L, true));
        categories.putIfAbsent(1L, new ProfessionalCategory(1L, "Clinica medica", "General", true));
        changes.put(version, new VersionChanges(version, List.of(), List.of(professionalId), List.of()));
        currentVersion = Math.max(currentVersion, version);
    }

    void localAt(long version) {
        SyncState s = SyncState.neverSynced();
        s.markUpToDate(version, NOW);
        state = s;
    }

    private static SyncState copy(SyncState s) {
        return new SyncState(s.getId(), s.getLastSnapshotVersion(), s.getLastSyncedAt(), s.getStatus(), s.getLastError());
    }
}

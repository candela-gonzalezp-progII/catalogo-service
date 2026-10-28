package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.application.port.out.CatalogReplicaPort;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.domain.model.CatalogSnapshot;
import com.integrador.catalogo.domain.model.SyncState;
import com.integrador.catalogo.infrastructure.persistence.jpa.ProfessionalCategoryJpaRepository;
import com.integrador.catalogo.infrastructure.persistence.jpa.ProfessionalJpaRepository;
import com.integrador.catalogo.infrastructure.persistence.jpa.WeeklyScheduleJpaRepository;
import com.integrador.catalogo.infrastructure.persistence.mapper.ProfessionalCategoryMapper;
import com.integrador.catalogo.infrastructure.persistence.mapper.ProfessionalMapper;
import com.integrador.catalogo.infrastructure.persistence.mapper.WeeklyScheduleMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Aplica un snapshot completo en UNA transaccion: borra la replica, inserta categorias,
 * profesionales y horarios (en orden de FK) y recien al final registra la version local.
 * Si cualquier paso falla, Postgres hace rollback y la version local no avanza.
 */
@Component
public class CatalogReplicaAdapter implements CatalogReplicaPort {

    private final ProfessionalCategoryJpaRepository categories;
    private final ProfessionalJpaRepository professionals;
    private final WeeklyScheduleJpaRepository schedules;
    private final SyncStatePort syncStatePort;

    public CatalogReplicaAdapter(ProfessionalCategoryJpaRepository categories,
                                 ProfessionalJpaRepository professionals,
                                 WeeklyScheduleJpaRepository schedules,
                                 SyncStatePort syncStatePort) {
        this.categories = categories;
        this.professionals = professionals;
        this.schedules = schedules;
        this.syncStatePort = syncStatePort;
    }

    @Override
    @Transactional
    public void replaceWithSnapshot(CatalogSnapshot snapshot, Instant syncedAt) {
        clearReplica();
        insertSnapshot(snapshot);
        registerVersion(snapshot.version(), syncedAt);
    }

    private void clearReplica() {
        schedules.deleteAllInBatch();
        professionals.deleteAllInBatch();
        categories.deleteAllInBatch();
    }

    private void insertSnapshot(CatalogSnapshot snapshot) {
        categories.saveAll(snapshot.categories().stream().map(ProfessionalCategoryMapper::toEntity).toList());
        categories.flush();
        professionals.saveAll(snapshot.professionals().stream().map(ProfessionalMapper::toEntity).toList());
        professionals.flush();
        schedules.saveAll(snapshot.schedules().stream().map(WeeklyScheduleMapper::toEntity).toList());
        schedules.flush();
    }

    private void registerVersion(long version, Instant syncedAt) {
        SyncState state = syncStatePort.get();
        state.markUpToDate(version, syncedAt);
        syncStatePort.save(state);
    }
}

package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.application.port.out.CatalogVersionPort;
import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.domain.model.SyncState;
import com.integrador.catalogo.domain.model.VersionData;
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
 * Aplica UNA version incremental en una sola transaccion. save() con id asignado hace upsert,
 * asi que aplicar dos veces la misma version deja el mismo resultado (idempotente).
 * Las bajas son logicas: la catedra deja la entidad con enabled=false.
 */
@Component
public class CatalogVersionAdapter implements CatalogVersionPort {

    private final ProfessionalCategoryJpaRepository categories;
    private final ProfessionalJpaRepository professionals;
    private final WeeklyScheduleJpaRepository schedules;
    private final SyncStatePort syncStatePort;

    public CatalogVersionAdapter(ProfessionalCategoryJpaRepository categories, ProfessionalJpaRepository professionals,
                                 WeeklyScheduleJpaRepository schedules, SyncStatePort syncStatePort) {
        this.categories = categories;
        this.professionals = professionals;
        this.schedules = schedules;
        this.syncStatePort = syncStatePort;
    }

    @Override
    @Transactional
    public void applyVersion(VersionData data, Instant syncedAt) {
        categories.saveAll(data.categories().stream().map(ProfessionalCategoryMapper::toEntity).toList());
        categories.flush();
        professionals.saveAll(data.professionals().stream().map(ProfessionalMapper::toEntity).toList());
        professionals.flush();
        schedules.saveAll(data.schedules().stream().map(WeeklyScheduleMapper::toEntity).toList());
        schedules.flush();
        SyncState state = syncStatePort.get();
        state.markUpToDate(data.version(), syncedAt);
        syncStatePort.save(state);
    }
}

package com.integrador.catalogo.infrastructure.persistence.mapper;

import com.integrador.catalogo.domain.model.SyncState;
import com.integrador.catalogo.infrastructure.persistence.entity.SyncStateEntity;

public final class SyncStateMapper {

    private SyncStateMapper() {
    }

    public static SyncStateEntity toEntity(SyncState domain) {
        return new SyncStateEntity(
                domain.getId(), domain.getLastSnapshotVersion(),
                domain.getLastSyncedAt(), domain.getStatus()
        );
    }

    public static SyncState toDomain(SyncStateEntity entity) {
        return new SyncState(
                entity.getId(), entity.getLastSnapshotVersion(),
                entity.getLastSyncedAt(), entity.getStatus()
        );
    }
}
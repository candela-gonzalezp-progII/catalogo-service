package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.application.port.out.SyncStatePort;
import com.integrador.catalogo.domain.model.SyncState;
import com.integrador.catalogo.infrastructure.persistence.jpa.SyncStateJpaRepository;
import com.integrador.catalogo.infrastructure.persistence.mapper.SyncStateMapper;
import org.springframework.stereotype.Component;

@Component
public class SyncStateRepositoryAdapter implements SyncStatePort {

    private final SyncStateJpaRepository jpaRepository;

    public SyncStateRepositoryAdapter(SyncStateJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public SyncState get() {
        return jpaRepository.findById(SyncState.SINGLETON_ID)
                .map(SyncStateMapper::toDomain)
                .orElseGet(SyncState::neverSynced);
    }

    @Override
    public SyncState save(SyncState state) {
        var saved = jpaRepository.save(SyncStateMapper.toEntity(state));
        return SyncStateMapper.toDomain(saved);
    }
}

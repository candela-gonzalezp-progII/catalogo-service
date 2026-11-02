package com.integrador.catalogo.infrastructure.persistence.adapter;

import com.integrador.catalogo.application.port.out.ProcessedEventPort;
import com.integrador.catalogo.infrastructure.persistence.entity.ProcessedEventEntity;
import com.integrador.catalogo.infrastructure.persistence.jpa.ProcessedEventJpaRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProcessedEventAdapter implements ProcessedEventPort {

    private final ProcessedEventJpaRepository repository;

    public ProcessedEventAdapter(ProcessedEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean wasProcessed(String eventId) {
        return repository.existsById(eventId);
    }

    @Override
    public void markProcessed(String eventId, Instant at) {
        repository.save(new ProcessedEventEntity(eventId, at));
    }
}

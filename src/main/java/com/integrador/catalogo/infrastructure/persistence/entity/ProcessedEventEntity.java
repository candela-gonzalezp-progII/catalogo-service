package com.integrador.catalogo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "processed_event")
@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class ProcessedEventEntity {

    @Id
    @Column(name = "event_id", length = 200)
    private String eventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;
}

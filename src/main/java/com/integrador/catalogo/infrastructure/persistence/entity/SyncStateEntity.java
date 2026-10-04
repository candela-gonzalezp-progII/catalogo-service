package com.integrador.catalogo.infrastructure.persistence.entity;

import com.integrador.catalogo.domain.model.SyncStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "sync_state")
@Getter
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class SyncStateEntity {

    @Id
    private Long id;

    @Column(name = "last_snapshot_version")
    private Long lastSnapshotVersion;

    @Column(name = "last_synced_at")
    private Instant lastSyncedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SyncStatus status;
}
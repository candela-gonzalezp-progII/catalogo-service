package com.integrador.catalogo.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SyncMetadataTest {

    private final SyncMetadata metadata = new SyncMetadata(7, 4, null);

    @Test
    void requiereSnapshotSiLaVersionLocalEstaFueraDeLaVentana() {
        assertThat(metadata.requiresSnapshot(3)).isTrue();
    }

    @Test
    void noRequiereSnapshotSiLaVersionLocalEstaDentroDeLaVentana() {
        assertThat(metadata.requiresSnapshot(4)).isFalse();
        assertThat(metadata.requiresSnapshot(7)).isFalse();
    }

    @Test
    void estaAtrasadoSoloSiLaVersionLocalEsMenorALaActual() {
        assertThat(metadata.isBehind(6)).isTrue();
        assertThat(metadata.isBehind(7)).isFalse();
    }

    @Test
    void rechazaUnaVentanaInconsistente() {
        assertThatThrownBy(() -> new SyncMetadata(3, 4, null)).isInstanceOf(IllegalArgumentException.class);
    }
}

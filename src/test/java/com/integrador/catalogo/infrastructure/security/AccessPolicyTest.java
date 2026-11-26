package com.integrador.catalogo.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AccessPolicyTest {

    private final AccessPolicy policy = new AccessPolicy();

    @Test
    void consultasAdmitenUsuarioYServicio() {
        Requirement r = policy.requirementFor("GET", "/api/professionals");

        assertThat(r.allows(Set.of("ROLE_USER"))).isEqualTo(true);
        assertThat(r.allows(Set.of("ROLE_SERVICE"))).isEqualTo(true);
        assertThat(r.allows(Set.of())).isEqualTo(false);
    }

    @Test
    void syncEsSoloParaServicios() {
        Requirement r = policy.requirementFor("GET", "/api/sync/status");

        assertThat(r.allows(Set.of("ROLE_SERVICE"))).isEqualTo(true);
        assertThat(r.allows(Set.of("ROLE_USER"))).isEqualTo(false);
        assertThat(r.allows(Set.of("ROLE_ADMIN"))).isEqualTo(false);
    }

    @Test
    void nadaEsPublicoSalvoOptions() {
        assertThat(policy.requirementFor("GET", "/api/categories").isPublic()).isEqualTo(false);
        assertThat(policy.requirementFor("OPTIONS", "/api/categories").isPublic()).isEqualTo(true);
    }
}

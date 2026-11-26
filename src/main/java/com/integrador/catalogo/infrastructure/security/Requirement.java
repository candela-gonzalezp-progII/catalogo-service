package com.integrador.catalogo.infrastructure.security;

import java.util.Set;

/** Que exige una ruta: ser publica, o presentar un token con alguna de estas autoridades. */
public record Requirement(boolean isPublic, Set<String> anyOf) {

    public static Requirement publicAccess() {
        return new Requirement(true, Set.of());
    }

    public static Requirement anyOf(String... authorities) {
        return new Requirement(false, Set.of(authorities));
    }

    public boolean allows(Set<String> granted) {
        return isPublic || granted.stream().anyMatch(anyOf::contains);
    }
}

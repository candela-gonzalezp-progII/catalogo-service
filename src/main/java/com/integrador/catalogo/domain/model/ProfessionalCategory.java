package com.integrador.catalogo.domain.model;

/**
 * Categoria profesional del catalogo (ej: "Medicina General", "Odontologia").
 * Clase de dominio pura: sin anotaciones de framework, sin dependencias externas.
 */
public class ProfessionalCategory {

    private final Long id;
    private String name;
    private String description;
    private boolean active;

    public ProfessionalCategory(Long id, String name, String description, boolean active) {
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name no puede estar vacio");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }

    public void rename(String newName) {
        if (newName == null || newName.isBlank()) {
            throw new IllegalArgumentException("name no puede estar vacio");
        }
        this.name = newName;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}

package com.integrador.catalogo.domain.model;

/**
 * Profesional dentro de una categoria del catalogo.
 */
public class Professional {

    private final Long id;
    private String firstName;
    private String lastName;
    private final Long categoryId;
    private boolean active;

    public Professional(Long id, String firstName, String lastName, Long categoryId, boolean active) {
        if (id == null) {
            throw new IllegalArgumentException("id no puede ser null");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("firstName no puede estar vacio");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("lastName no puede estar vacio");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("categoryId no puede ser null");
        }
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.categoryId = categoryId;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public boolean isActive() {
        return active;
    }

    public String fullName() {
        return firstName + " " + lastName;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }
}

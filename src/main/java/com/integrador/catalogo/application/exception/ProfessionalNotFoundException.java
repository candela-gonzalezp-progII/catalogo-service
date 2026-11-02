package com.integrador.catalogo.application.exception;

public class ProfessionalNotFoundException extends RuntimeException {

    public ProfessionalNotFoundException(Long id) {
        super("No existe el profesional " + id);
    }
}

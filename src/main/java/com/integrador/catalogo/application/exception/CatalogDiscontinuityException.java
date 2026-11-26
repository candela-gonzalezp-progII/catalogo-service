package com.integrador.catalogo.application.exception;

/**
 * No se puede continuar la sincronizacion incremental de forma segura (falta una version,
 * la version local quedo fuera de la ventana de Redis, o no hay version local).
 * La solucion es reconstruir desde un snapshot (CU-07).
 */
public class CatalogDiscontinuityException extends RuntimeException {

    public CatalogDiscontinuityException(String message) {
        super(message);
    }
}

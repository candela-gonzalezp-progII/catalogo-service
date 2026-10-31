package com.integrador.catalogo.application.exception;

public class SynchronizationInProgressException extends RuntimeException {

    public SynchronizationInProgressException() {
        super("Ya hay una sincronizacion en curso");
    }
}

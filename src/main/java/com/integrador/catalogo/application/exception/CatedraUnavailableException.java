package com.integrador.catalogo.application.exception;

/** La catedra no respondio a tiempo o devolvio 5xx: es un error transitorio, se puede reintentar. */
public class CatedraUnavailableException extends CatedraIntegrationException {

    public CatedraUnavailableException(String message, int httpStatus, String code, Throwable cause) {
        super(message, httpStatus, code, cause);
    }
}

package com.integrador.catalogo.application.exception;

/** Error al integrar con la catedra. Lleva el status HTTP y el code funcional (si los hay). */
public class CatedraIntegrationException extends RuntimeException {

    private final int httpStatus;
    private final String code;

    public CatedraIntegrationException(String message, int httpStatus, String code, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.code = code;
    }

    public CatedraIntegrationException(String message) {
        this(message, 0, null, null);
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getCode() {
        return code;
    }
}

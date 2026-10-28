package com.integrador.catalogo.application.exception;

/** La catedra rechazo la cuenta tecnica o el token (401/403). No se reintenta a ciegas. */
public class CatedraAuthenticationException extends CatedraIntegrationException {

    public CatedraAuthenticationException(String message, int httpStatus, String code, Throwable cause) {
        super(message, httpStatus, code, cause);
    }
}

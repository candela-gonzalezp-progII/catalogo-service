package com.integrador.catalogo.infrastructure.catedra;

import com.integrador.catalogo.application.exception.CatedraAuthenticationException;
import com.integrador.catalogo.application.exception.CatedraIntegrationException;
import com.integrador.catalogo.application.exception.CatedraUnavailableException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;

/** Traduce los errores HTTP del cliente a las excepciones propias de la aplicacion. */
final class CatedraHttpSupport {

    private CatedraHttpSupport() {
    }

    static RuntimeException translate(RuntimeException error) {
        if (error instanceof RestClientResponseException response) {
            return translateResponse(response);
        }
        if (error instanceof ResourceAccessException) {
            return new CatedraUnavailableException("No se pudo conectar con la catedra (timeout o red)", 0, null, error);
        }
        return error;
    }

    private static RuntimeException translateResponse(RestClientResponseException response) {
        int status = response.getStatusCode().value();
        String code = ProblemCodeExtractor.extract(response.getResponseBodyAsString());
        String message = "La catedra respondio HTTP " + status + (code != null ? " (" + code + ")" : "");
        if (status == 401 || status == 403) {
            return new CatedraAuthenticationException(message, status, code, response);
        }
        if (status >= 500 || status == 408 || status == 429) {
            return new CatedraUnavailableException(message, status, code, response);
        }
        return new CatedraIntegrationException(message, status, code, response);
    }
}

package com.integrador.catalogo.infrastructure.catedra;

import com.integrador.catalogo.application.exception.CatedraAuthenticationException;
import com.integrador.catalogo.application.exception.CatedraIntegrationException;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

/**
 * Cliente GET autenticado con el JWT tecnico: agrega el Bearer, reintenta errores transitorios
 * (acotado) y, si la catedra responde 401/403, vuelve a autenticar UNA sola vez.
 */
public class CatedraApiClient {

    private final RestClient restClient;
    private final CatedraTokenProvider tokens;
    private final RetryExecutor retry;

    public CatedraApiClient(RestClient restClient, CatedraTokenProvider tokens, RetryExecutor retry) {
        this.restClient = restClient;
        this.tokens = tokens;
        this.retry = retry;
    }

    public <T> T get(String uri, Class<T> type, Object... uriVariables) {
        try {
            return getOnce(uri, type, uriVariables);
        } catch (CatedraAuthenticationException e) {
            tokens.invalidate();
            return getOnce(uri, type, uriVariables);
        }
    }

    private <T> T getOnce(String uri, Class<T> type, Object... uriVariables) {
        T body = retry.execute(() -> request(uri, type, uriVariables));
        if (body == null) {
            throw new CatedraIntegrationException("La catedra devolvio una respuesta vacia en " + uri);
        }
        return body;
    }

    private <T> T request(String uri, Class<T> type, Object... uriVariables) {
        try {
            return restClient.get()
                    .uri(uri, uriVariables)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.bearerToken())
                    .retrieve()
                    .body(type);
        } catch (RuntimeException e) {
            throw CatedraHttpSupport.translate(e);
        }
    }
}

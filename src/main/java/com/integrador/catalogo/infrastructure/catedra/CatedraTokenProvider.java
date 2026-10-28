package com.integrador.catalogo.infrastructure.catedra;

import com.integrador.catalogo.application.exception.CatedraIntegrationException;
import com.integrador.catalogo.domain.model.TechnicalSession;
import com.integrador.catalogo.infrastructure.catedra.dto.AuthRequest;
import com.integrador.catalogo.infrastructure.catedra.dto.AuthResponse;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

/**
 * Obtiene y conserva en memoria el JWT tecnico de la catedra (vigencia ~1 anio).
 * El token no se escribe en disco, ni en logs, ni se expone a la app Android.
 */
public class CatedraTokenProvider {

    private final RestClient restClient;
    private final CatedraProperties properties;
    private final RetryExecutor retry;
    private TechnicalSession session;

    public CatedraTokenProvider(RestClient restClient, CatedraProperties properties, RetryExecutor retry) {
        this.restClient = restClient;
        this.properties = properties;
        this.retry = retry;
    }

    /** Hace login tecnico (POST /api/authenticate) y guarda la sesion. */
    public synchronized TechnicalSession authenticate() {
        AuthResponse response = retry.execute(this::postAuthenticate);
        if (response == null || response.idToken() == null || response.idToken().isBlank()) {
            throw new CatedraIntegrationException("La catedra no devolvio un token valido");
        }
        session = response.toSession();
        return session;
    }

    public synchronized String bearerToken() {
        return session != null ? session.token() : authenticate().token();
    }

    /** Descarta el token (por ejemplo, tras un 401) para obligar a re-autenticar. */
    public synchronized void invalidate() {
        session = null;
    }

    private AuthResponse postAuthenticate() {
        try {
            return restClient.post()
                    .uri("/api/authenticate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new AuthRequest(properties.username(), properties.password(), true))
                    .retrieve()
                    .body(AuthResponse.class);
        } catch (RuntimeException e) {
            throw CatedraHttpSupport.translate(e);
        }
    }
}

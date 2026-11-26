package com.integrador.catalogo.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final Instant NOW = Instant.parse("2026-11-05T12:00:00Z");

    private HmacJwtService jwt;
    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        jwt = new HmacJwtService("un-secreto-de-prueba-bastante-largo-1234567890", "turnos-service", Clock.fixed(NOW, ZoneOffset.UTC));
        filter = new JwtAuthenticationFilter(jwt, new AccessPolicy(), new ProblemJsonWriter());
    }

    private String token(String... authorities) {
        return jwt.sign(new JwtClaims("cande", Set.of(authorities), "turnos-service", NOW, NOW.plusSeconds(60), null));
    }

    private MockHttpServletResponse call(String method, String uri, String authHeader, MockFilterChain chain) throws Exception {
        MockHttpServletRequest req = new MockHttpServletRequest(method, uri);
        if (authHeader != null) {
            req.addHeader("Authorization", authHeader);
        }
        MockHttpServletResponse res = new MockHttpServletResponse();
        filter.doFilter(req, res, chain);
        return res;
    }

    @Test
    void rutaPublicaPasaSinToken() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse res = call("OPTIONS", "/api/professionals", null, chain);

        assertThat(res.getStatus()).isEqualTo(200);
        assertThat(chain.getRequest() != null).isEqualTo(true);
    }

    @Test
    void sinTokenDa401ConProblemJson() throws Exception {
        MockFilterChain chain = new MockFilterChain();

        MockHttpServletResponse res = call("GET", "/api/professionals", null, chain);

        assertThat(res.getStatus()).isEqualTo(401);
        assertThat(res.getContentAsString().contains("\"code\":\"UNAUTHORIZED\"")).isEqualTo(true);
        assertThat(chain.getRequest() == null).isEqualTo(true);
    }

    @Test
    void tokenInvalidoDa401() throws Exception {
        assertThat(call("GET", "/api/professionals", "Bearer basura", new MockFilterChain()).getStatus()).isEqualTo(401);
    }

    @Test
    void tokenDeUsuarioEnRutaDeSyncDa403() throws Exception {
        MockHttpServletResponse res = call("GET", "/api/sync/status", "Bearer " + token("ROLE_USER"), new MockFilterChain());

        assertThat(res.getStatus()).isEqualTo(403);
        assertThat(res.getContentAsString().contains("\"code\":\"FORBIDDEN\"")).isEqualTo(true);
    }

    @Test
    void tokenDeServicioPuedeLeerSync() throws Exception {
        assertThat(call("GET", "/api/sync/status", "Bearer " + token("ROLE_SERVICE"), new MockFilterChain()).getStatus()).isEqualTo(200);
    }

    @Test
    void tokenDeUsuarioPasaYDejaElLogin() throws Exception {
        MockFilterChain chain = new MockFilterChain();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/professionals");
        req.addHeader("Authorization", "Bearer " + token("ROLE_USER"));

        filter.doFilter(req, new MockHttpServletResponse(), chain);

        assertThat(req.getAttribute(AuthAttributes.LOGIN)).isEqualTo("cande");
        assertThat(chain.getRequest() != null).isEqualTo(true);
    }
}

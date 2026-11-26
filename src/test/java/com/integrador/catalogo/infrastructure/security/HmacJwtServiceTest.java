package com.integrador.catalogo.infrastructure.security;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HmacJwtServiceTest {

    private static final String SECRET = "un-secreto-de-prueba-bastante-largo-1234567890";
    private static final Instant NOW = Instant.parse("2026-11-05T12:00:00Z");

    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);
    private final HmacJwtService jwt = new HmacJwtService(SECRET, "turnos-service", clock);

    private JwtClaims claims(Instant exp) {
        return new JwtClaims("cande", Set.of("ROLE_USER"), "turnos-service", NOW, exp, null);
    }

    private static String b64(String s) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void firmaYVerificaIdaYVuelta() {
        JwtClaims out = jwt.verify(jwt.sign(claims(NOW.plusSeconds(3600))));

        assertThat(out.subject()).isEqualTo("cande");
        assertThat(out.authorities()).contains("ROLE_USER");
        assertThat(out.expiresAt()).isEqualTo(NOW.plusSeconds(3600));
    }

    @Test
    void conservaOnBehalfOf() {
        JwtClaims c = new JwtClaims("turnos-service", Set.of("ROLE_SERVICE"), "turnos-service", NOW, NOW.plusSeconds(60), "cande");

        assertThat(jwt.verify(jwt.sign(c)).onBehalfOf()).isEqualTo("cande");
    }

    @Test
    void rechazaTokenVencido() {
        String token = jwt.sign(claims(NOW.minusSeconds(1)));

        assertThatThrownBy(() -> jwt.verify(token)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rechazaFirmaAdulterada() {
        String[] p = jwt.sign(claims(NOW.plusSeconds(3600))).split("\\.");
        String forged = p[0] + "." + b64("{\"sub\":\"admin\",\"iss\":\"turnos-service\",\"iat\":1,\"exp\":9999999999,\"auth\":[\"ROLE_ADMIN\"]}") + "." + p[2];

        assertThatThrownBy(() -> jwt.verify(forged)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rechazaOtroSecreto() {
        HmacJwtService other = new HmacJwtService("otro-secreto-distinto-de-mas-de-32-caracteres", "turnos-service", clock);

        assertThatThrownBy(() -> jwt.verify(other.sign(claims(NOW.plusSeconds(60))))).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rechazaAlgNone() {
        String token = b64("{\"alg\":\"none\",\"typ\":\"JWT\"}") + "."
                + b64("{\"sub\":\"cande\",\"iss\":\"turnos-service\",\"iat\":1,\"exp\":9999999999,\"auth\":[\"ROLE_ADMIN\"]}") + ".";

        assertThatThrownBy(() -> jwt.verify(token)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rechazaOtroEmisor() {
        HmacJwtService other = new HmacJwtService(SECRET, "otro-emisor", clock);
        String token = other.sign(new JwtClaims("cande", Set.of("ROLE_USER"), "otro-emisor", NOW, NOW.plusSeconds(60), null));

        assertThatThrownBy(() -> jwt.verify(token)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rechazaBasuraYVacio() {
        assertThatThrownBy(() -> jwt.verify("esto.no.es-un-jwt")).isInstanceOf(InvalidTokenException.class);
        assertThatThrownBy(() -> jwt.verify("")).isInstanceOf(InvalidTokenException.class);
        assertThatThrownBy(() -> jwt.verify(null)).isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void exigeSecretoDeAlMenos32Caracteres() {
        assertThatThrownBy(() -> new HmacJwtService("corto", "turnos-service", clock)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> new HmacJwtService("", "turnos-service", clock)).isInstanceOf(IllegalStateException.class);
    }
}

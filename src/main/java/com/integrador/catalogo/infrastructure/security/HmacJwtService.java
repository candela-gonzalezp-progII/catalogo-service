package com.integrador.catalogo.infrastructure.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JWT firmado con HS256 (HMAC-SHA256) y un secreto compartido por catalogo y turnos.
 * Solo acepta HS256 (rechaza "alg":"none"), exige firma valida, emisor esperado y que no este vencido.
 */
public class HmacJwtService {

    private static final String HEADER_JSON = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    private static final String ALGORITHM = "HmacSHA256";
    public static final int MIN_SECRET_BYTES = 32;

    private final byte[] secret;
    private final String issuer;
    private final Clock clock;

    public HmacJwtService(String secret, String issuer, Clock clock) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "security.jwt.secret (JWT_SECRET) debe tener al menos " + MIN_SECRET_BYTES + " caracteres");
        }
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("security.jwt.issuer es obligatorio");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.issuer = issuer;
        this.clock = clock;
    }

    public String issuer() {
        return issuer;
    }

    public Clock clock() {
        return clock;
    }

    public String sign(JwtClaims claims) {
        StringBuilder payload = new StringBuilder("{");
        payload.append("\"sub\":").append(JsonMini.quote(claims.subject()));
        payload.append(",\"iss\":").append(JsonMini.quote(claims.issuer()));
        payload.append(",\"iat\":").append(claims.issuedAt().getEpochSecond());
        payload.append(",\"exp\":").append(claims.expiresAt().getEpochSecond());
        payload.append(",\"auth\":[");
        boolean first = true;
        for (String a : new java.util.TreeSet<>(claims.authorities())) {
            if (!first) {
                payload.append(',');
            }
            payload.append(JsonMini.quote(a));
            first = false;
        }
        payload.append(']');
        if (claims.onBehalfOf() != null) {
            payload.append(",\"obo\":").append(JsonMini.quote(claims.onBehalfOf()));
        }
        payload.append('}');

        String signingInput = b64(HEADER_JSON.getBytes(StandardCharsets.UTF_8))
                + "." + b64(payload.toString().getBytes(StandardCharsets.UTF_8));
        return signingInput + "." + b64(hmac(signingInput));
    }

    /** @throws InvalidTokenException por cualquier motivo de rechazo (el mensaje es interno, no se muestra al cliente) */
    public JwtClaims verify(String token) {
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("token vacio");
        }
        String[] parts = token.split("\\.", -1);
        if (parts.length != 3) {
            throw new InvalidTokenException("formato invalido");
        }
        try {
            Map<String, Object> header = JsonMini.parseObject(decode(parts[0]));
            if (!"HS256".equals(header.get("alg"))) {
                throw new InvalidTokenException("algoritmo no permitido");
            }
            byte[] expected = hmac(parts[0] + "." + parts[1]);
            byte[] given = Base64.getUrlDecoder().decode(parts[2]);
            if (!MessageDigest.isEqual(expected, given)) {
                throw new InvalidTokenException("firma invalida");
            }
            Map<String, Object> p = JsonMini.parseObject(decode(parts[1]));
            JwtClaims claims = toClaims(p);
            if (!issuer.equals(claims.issuer())) {
                throw new InvalidTokenException("emisor inesperado");
            }
            if (!clock.instant().isBefore(claims.expiresAt())) {
                throw new InvalidTokenException("token vencido");
            }
            return claims;
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("token mal formado");
        }
    }

    @SuppressWarnings("unchecked")
    private static JwtClaims toClaims(Map<String, Object> p) {
        Object sub = p.get("sub");
        Object iss = p.get("iss");
        Object iat = p.get("iat");
        Object exp = p.get("exp");
        Object auth = p.get("auth");
        Object obo = p.get("obo");
        if (!(sub instanceof String s) || s.isBlank() || !(iss instanceof String i)
                || !(iat instanceof Long issued) || !(exp instanceof Long expires) || !(auth instanceof List<?>)) {
            throw new InvalidTokenException("faltan datos obligatorios");
        }
        Set<String> authorities = new LinkedHashSet<>((List<String>) auth);
        if (authorities.isEmpty()) {
            throw new InvalidTokenException("sin autoridades");
        }
        return new JwtClaims(s, authorities, i, java.time.Instant.ofEpochSecond(issued),
                java.time.Instant.ofEpochSecond(expires), obo instanceof String o ? o : null);
    }

    private byte[] hmac(String data) {
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(new SecretKeySpec(secret, ALGORITHM));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo firmar", e);
        }
    }

    private static String b64(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String decode(String part) {
        return new String(Base64.getUrlDecoder().decode(part), StandardCharsets.UTF_8);
    }
}

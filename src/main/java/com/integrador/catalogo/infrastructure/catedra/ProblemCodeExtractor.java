package com.integrador.catalogo.infrastructure.catedra;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Extrae el "code" funcional de una respuesta application/problem+json de la catedra. */
final class ProblemCodeExtractor {

    private static final Pattern CODE = Pattern.compile("\"code\"\\s*:\\s*\"([A-Z0-9_]+)\"");

    private ProblemCodeExtractor() {
    }

    /** @return el code, o null si el body no lo trae (ej. 401/403 de Spring Security). */
    static String extract(String body) {
        if (body == null) {
            return null;
        }
        Matcher matcher = CODE.matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }
}

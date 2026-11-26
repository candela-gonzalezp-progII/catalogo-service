package com.integrador.catalogo.infrastructure.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Lector/escritor de JSON minimo y estricto, SOLO para el JWT: un objeto plano cuyos valores son
 * texto, enteros o listas de texto. Evita depender de una version concreta de Jackson en la parte de seguridad.
 */
final class JsonMini {

    private final String s;
    private int i;

    private JsonMini(String s) {
        this.s = s;
    }

    static Map<String, Object> parseObject(String json) {
        JsonMini p = new JsonMini(json);
        p.skipWs();
        Map<String, Object> result = p.readObject();
        p.skipWs();
        if (p.i != p.s.length()) {
            throw new IllegalArgumentException("sobra contenido");
        }
        return result;
    }

    static String quote(String text) {
        StringBuilder b = new StringBuilder("\"");
        for (char c : text.toCharArray()) {
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < 0x20) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }
        return b.append('"').toString();
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> map = new LinkedHashMap<>();
        skipWs();
        if (peek() == '}') {
            i++;
            return map;
        }
        while (true) {
            skipWs();
            String key = readString();
            skipWs();
            expect(':');
            skipWs();
            if (map.put(key, readValue()) != null) {
                throw new IllegalArgumentException("clave repetida: " + key);
            }
            skipWs();
            char c = next();
            if (c == '}') {
                return map;
            }
            if (c != ',') {
                throw new IllegalArgumentException("se esperaba , o }");
            }
        }
    }

    private Object readValue() {
        char c = peek();
        if (c == '"') {
            return readString();
        }
        if (c == '[') {
            return readArray();
        }
        if (c == '-' || Character.isDigit(c)) {
            return readLong();
        }
        throw new IllegalArgumentException("valor no soportado");
    }

    private List<String> readArray() {
        expect('[');
        List<String> list = new ArrayList<>();
        skipWs();
        if (peek() == ']') {
            i++;
            return list;
        }
        while (true) {
            skipWs();
            list.add(readString());
            skipWs();
            char c = next();
            if (c == ']') {
                return list;
            }
            if (c != ',') {
                throw new IllegalArgumentException("se esperaba , o ]");
            }
        }
    }

    private Long readLong() {
        int start = i;
        if (peek() == '-') {
            i++;
        }
        while (i < s.length() && Character.isDigit(s.charAt(i))) {
            i++;
        }
        if (i == start || (i - start == 1 && s.charAt(start) == '-')) {
            throw new IllegalArgumentException("numero invalido");
        }
        return Long.parseLong(s.substring(start, i));
    }

    private String readString() {
        expect('"');
        StringBuilder b = new StringBuilder();
        while (true) {
            char c = next();
            if (c == '"') {
                return b.toString();
            }
            if (c != '\\') {
                b.append(c);
                continue;
            }
            char e = next();
            switch (e) {
                case '"', '\\', '/' -> b.append(e);
                case 'n' -> b.append('\n');
                case 'r' -> b.append('\r');
                case 't' -> b.append('\t');
                case 'b' -> b.append('\b');
                case 'f' -> b.append('\f');
                case 'u' -> {
                    if (i + 4 > s.length()) {
                        throw new IllegalArgumentException("escape u incompleto");
                    }
                    b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                    i += 4;
                }
                default -> throw new IllegalArgumentException("escape invalido");
            }
        }
    }

    private void skipWs() {
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
            i++;
        }
    }

    private char peek() {
        if (i >= s.length()) {
            throw new IllegalArgumentException("fin inesperado");
        }
        return s.charAt(i);
    }

    private char next() {
        char c = peek();
        i++;
        return c;
    }

    private void expect(char c) {
        if (next() != c) {
            throw new IllegalArgumentException("se esperaba " + c);
        }
    }
}

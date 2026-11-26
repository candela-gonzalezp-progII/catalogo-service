package com.integrador.catalogo.infrastructure.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Escribe 401/403 como application/problem+json con "code" estable (el filtro corre antes de los controladores). */
public class ProblemJsonWriter {

    public void unauthorized(HttpServletResponse response) throws IOException {
        write(response, 401, "Unauthorized", "UNAUTHORIZED", "Falta el token o no es valido");
    }

    public void forbidden(HttpServletResponse response) throws IOException {
        write(response, 403, "Forbidden", "FORBIDDEN", "No tiene permisos para este recurso");
    }

    private void write(HttpServletResponse response, int status, String title, String code, String detail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        if (status == 401) {
            response.setHeader("WWW-Authenticate", "Bearer");
        }
        String body = "{\"type\":\"about:blank\",\"title\":\"" + title + "\",\"status\":" + status
                + ",\"detail\":\"" + detail + "\",\"code\":\"" + code + "\"}";
        response.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}

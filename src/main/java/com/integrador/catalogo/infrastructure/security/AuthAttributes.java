package com.integrador.catalogo.infrastructure.security;

/** Nombres de los atributos de la peticion que deja el filtro tras validar el token. */
public final class AuthAttributes {

    public static final String LOGIN = "catalogo.auth.login";
    public static final String AUTHORITIES = "catalogo.auth.authorities";

    private AuthAttributes() {
    }
}

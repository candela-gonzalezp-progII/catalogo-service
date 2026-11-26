package com.integrador.catalogo.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Valida el token "Authorization: Bearer ..." y aplica la AccessPolicy.
 * Sin token o token invalido -> 401. Token valido pero sin la autoridad requerida -> 403.
 * Nunca registra el token ni el secreto.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final HmacJwtService jwt;
    private final AccessPolicy policy;
    private final ProblemJsonWriter problems;

    public JwtAuthenticationFilter(HmacJwtService jwt, AccessPolicy policy, ProblemJsonWriter problems) {
        this.jwt = jwt;
        this.policy = policy;
        this.problems = problems;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Requirement required = policy.requirementFor(request.getMethod(), request.getRequestURI());
        if (required.isPublic()) {
            chain.doFilter(request, response);
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith(BEARER)) {
            problems.unauthorized(response);
            return;
        }
        JwtClaims claims;
        try {
            claims = jwt.verify(header.substring(BEARER.length()).trim());
        } catch (InvalidTokenException e) {
            problems.unauthorized(response);
            return;
        }
        if (!required.allows(claims.authorities())) {
            problems.forbidden(response);
            return;
        }
        request.setAttribute(AuthAttributes.LOGIN, claims.subject());
        request.setAttribute(AuthAttributes.AUTHORITIES, claims.authorities());
        chain.doFilter(request, response);
    }
}

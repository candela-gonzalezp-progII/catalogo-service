package com.integrador.catalogo.infrastructure.config;

import com.integrador.catalogo.infrastructure.security.AccessPolicy;
import com.integrador.catalogo.infrastructure.security.HmacJwtService;
import com.integrador.catalogo.infrastructure.security.JwtAuthenticationFilter;
import com.integrador.catalogo.infrastructure.security.JwtProperties;
import com.integrador.catalogo.infrastructure.security.ProblemJsonWriter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    /** Falla al arrancar si JWT_SECRET falta o es corto (menos de 32 caracteres). */
    @Bean
    HmacJwtService hmacJwtService(JwtProperties props) {
        return new HmacJwtService(props.secret(), props.issuer(), Clock.systemUTC());
    }

    @Bean
    FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter(HmacJwtService jwt) {
        FilterRegistrationBean<JwtAuthenticationFilter> reg =
                new FilterRegistrationBean<>(new JwtAuthenticationFilter(jwt, new AccessPolicy(), new ProblemJsonWriter()));
        reg.addUrlPatterns("/api/*");
        reg.setOrder(1);
        return reg;
    }
}

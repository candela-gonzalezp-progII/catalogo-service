package com.integrador.catalogo.infrastructure.config;

import com.integrador.catalogo.application.port.out.ProfessionalCategoryRepositoryPort;
import com.integrador.catalogo.application.port.out.ProfessionalSearchPort;
import com.integrador.catalogo.application.service.ProfessionalQueryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueryConfig {

    @Bean
    ProfessionalQueryService professionalQueryService(ProfessionalSearchPort searchPort,
                                                      ProfessionalCategoryRepositoryPort categoryPort) {
        return new ProfessionalQueryService(searchPort, categoryPort);
    }
}
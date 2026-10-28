package com.integrador.catalogo.infrastructure.config;

import com.integrador.catalogo.infrastructure.catedra.CatedraApiClient;
import com.integrador.catalogo.infrastructure.catedra.CatedraSnapshotAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CatedraSnapshotConfig {

    @Bean
    CatedraSnapshotAdapter catedraSnapshotAdapter(CatedraApiClient api) {
        return new CatedraSnapshotAdapter(api);
    }
}

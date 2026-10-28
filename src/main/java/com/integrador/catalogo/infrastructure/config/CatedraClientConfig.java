package com.integrador.catalogo.infrastructure.config;

import com.integrador.catalogo.infrastructure.catedra.CatedraApiClient;
import com.integrador.catalogo.infrastructure.catedra.CatedraAuthAdapter;
import com.integrador.catalogo.infrastructure.catedra.CatedraProperties;
import com.integrador.catalogo.infrastructure.catedra.CatedraTokenProvider;
import com.integrador.catalogo.infrastructure.catedra.RetryExecutor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;

/** Arma el cliente REST de la catedra con timeouts explicitos y reintentos acotados. */
@Configuration
@EnableConfigurationProperties(CatedraProperties.class)
public class CatedraClientConfig {

    @Bean
    RestClient catedraRestClient(CatedraProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.readTimeout());
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }

    @Bean
    RetryExecutor catedraRetryExecutor(CatedraProperties properties) {
        return new RetryExecutor(properties.maxAttempts(), properties.retryBackoff());
    }

    @Bean
    CatedraTokenProvider catedraTokenProvider(RestClient catedraRestClient, CatedraProperties properties,
                                              RetryExecutor catedraRetryExecutor) {
        return new CatedraTokenProvider(catedraRestClient, properties, catedraRetryExecutor);
    }

    @Bean
    CatedraApiClient catedraApiClient(RestClient catedraRestClient, CatedraTokenProvider tokens,
                                      RetryExecutor catedraRetryExecutor) {
        return new CatedraApiClient(catedraRestClient, tokens, catedraRetryExecutor);
    }

    @Bean
    CatedraAuthAdapter catedraAuthAdapter(CatedraTokenProvider tokens, CatedraApiClient api) {
        return new CatedraAuthAdapter(tokens, api);
    }
}

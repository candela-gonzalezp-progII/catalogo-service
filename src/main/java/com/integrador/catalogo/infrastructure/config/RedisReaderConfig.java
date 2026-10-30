package com.integrador.catalogo.infrastructure.config;

import com.integrador.catalogo.application.port.out.CatedraAuthPort;
import com.integrador.catalogo.application.port.out.CatedraRedisPort;
import com.integrador.catalogo.infrastructure.redis.RedisCatalogReader;
import com.integrador.catalogo.infrastructure.redis.RedisTemplateProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisReaderConfig {

    @Bean
    RedisTemplateProvider redisTemplateProvider(CatedraAuthPort authPort) {
        return new RedisTemplateProvider(authPort);
    }

    @Bean
    CatedraRedisPort catedraRedisPort(RedisTemplateProvider provider) {
        return new RedisCatalogReader(provider::get);
    }
}

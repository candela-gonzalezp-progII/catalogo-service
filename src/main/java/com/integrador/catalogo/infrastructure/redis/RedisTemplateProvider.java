package com.integrador.catalogo.infrastructure.redis;

import com.integrador.catalogo.application.exception.CatedraIntegrationException;
import com.integrador.catalogo.application.port.out.CatedraAuthPort;
import com.integrador.catalogo.domain.model.IntegrationConfig;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

/**
 * Crea (una vez) la conexion a Redis con los datos que entrega la catedra en /api/student/integration.
 * Si la cuenta aun no esta PROVISIONED no se intenta conectar.
 */
public class RedisTemplateProvider {

    private final CatedraAuthPort authPort;
    private StringRedisTemplate template;

    public RedisTemplateProvider(CatedraAuthPort authPort) {
        this.authPort = authPort;
    }

    public synchronized StringRedisTemplate get() {
        if (template == null) {
            template = build(authPort.fetchIntegration());
        }
        return template;
    }

    private static StringRedisTemplate build(IntegrationConfig config) {
        if (!config.isProvisioned()) {
            throw new CatedraIntegrationException(
                    "La integracion no esta PROVISIONED (estado " + config.provisioningStatus() + ")");
        }
        RedisStandaloneConfiguration standalone = new RedisStandaloneConfiguration(config.redisHost(), config.redisPort());
        standalone.setUsername(config.redisUsername());
        standalone.setPassword(RedisPassword.of(config.redisPassword()));
        LettuceClientConfiguration client = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(standalone, client);
        factory.afterPropertiesSet();
        return new StringRedisTemplate(factory);
    }
}

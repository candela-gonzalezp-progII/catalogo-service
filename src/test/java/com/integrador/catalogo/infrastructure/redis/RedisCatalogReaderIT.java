package com.integrador.catalogo.infrastructure.redis;

import com.integrador.catalogo.application.exception.CatedraIntegrationException;
import com.integrador.catalogo.domain.model.SyncMetadata;
import com.integrador.catalogo.domain.model.VersionChanges;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** CU-06 (base): lectura de versiones y cambios desde un Redis real con las claves del anexo. */
@Testcontainers
class RedisCatalogReaderIT {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static StringRedisTemplate template;

    @BeforeAll
    static void connect() {
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        factory.afterPropertiesSet();
        template = new StringRedisTemplate(factory);
    }

    @BeforeEach
    void clean() {
        template.delete(template.keys("*"));
    }

    private final RedisCatalogReader reader = new RedisCatalogReader(() -> template);

    @Test
    void leeLasVersionesPublicadasPorLaCatedra() {
        template.opsForValue().set("catedra:sync:current-version", "7");
        template.opsForValue().set("catedra:sync:oldest-available-version", "4");
        template.opsForHash().put("catedra:sync:metadata", "publishedAt", "2026-09-10T13:44:59Z");

        SyncMetadata metadata = reader.readMetadata();

        assertThat(metadata.currentVersion()).isEqualTo(7L);
        assertThat(metadata.oldestAvailableVersion()).isEqualTo(4L);
        assertThat(metadata.publishedAt()).isEqualTo(Instant.parse("2026-09-10T13:44:59Z"));
        assertThat(metadata.requiresSnapshot(3)).isTrue();
    }

    @Test
    void leeLosIdsAfectadosPorUnaVersion() {
        template.opsForValue().set("catedra:sync:changes:7", """
                {"version":7,"changes":{"professionalCategories":[],"professionals":[15],"weeklySchedules":[21,22]}}
                """);

        Optional<VersionChanges> changes = reader.readChanges(7);

        assertThat(changes).isPresent();
        assertThat(changes.get().professionalIds()).containsExactly(15L);
        assertThat(changes.get().scheduleIds()).containsExactly(21L, 22L);
        assertThat(changes.get().categoryIds()).isEmpty();
    }

    @Test
    void unaVersionFueraDeLaVentanaNoExiste() {
        assertThat(reader.readChanges(2)).isEmpty();
    }

    @Test
    void sinMetadataEnRedisSeInformaComoErrorDeIntegracion() {
        assertThatThrownBy(reader::readMetadata).isInstanceOf(CatedraIntegrationException.class);
    }

    @Test
    void rechazaUnJsonDeCambiosDeOtraVersion() {
        template.opsForValue().set("catedra:sync:changes:7",
                "{\"version\":9,\"changes\":{\"professionalCategories\":[],\"professionals\":[],\"weeklySchedules\":[]}}");

        assertThatThrownBy(() -> reader.readChanges(7)).isInstanceOf(CatedraIntegrationException.class);
    }
}

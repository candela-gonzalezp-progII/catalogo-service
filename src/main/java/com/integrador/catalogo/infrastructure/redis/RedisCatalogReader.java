package com.integrador.catalogo.infrastructure.redis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.integrador.catalogo.application.exception.CatedraIntegrationException;
import com.integrador.catalogo.application.exception.CatedraUnavailableException;
import com.integrador.catalogo.application.port.out.CatedraRedisPort;
import com.integrador.catalogo.domain.model.SyncMetadata;
import com.integrador.catalogo.domain.model.VersionChanges;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Lee de Redis el catalogo publicado por la catedra (claves catedra:sync:*, solo lectura).
 * Recibe un Supplier del template porque la conexion se arma recien con la configuracion
 * de integracion que entrega la catedra.
 */
public class RedisCatalogReader implements CatedraRedisPort {

    static final String CURRENT_VERSION_KEY = "catedra:sync:current-version";
    static final String OLDEST_VERSION_KEY = "catedra:sync:oldest-available-version";
    static final String METADATA_KEY = "catedra:sync:metadata";
    static final String CHANGES_KEY_PREFIX = "catedra:sync:changes:";

    private final Supplier<StringRedisTemplate> templateSupplier;
    private final JsonMapper json = JsonMapper.builder().build();

    public RedisCatalogReader(Supplier<StringRedisTemplate> templateSupplier) {
        this.templateSupplier = templateSupplier;
    }

    @Override
    public SyncMetadata readMetadata() {
        try {
            StringRedisTemplate redis = templateSupplier.get();
            long current = requiredLong(redis.opsForValue().get(CURRENT_VERSION_KEY), CURRENT_VERSION_KEY);
            long oldest = requiredLong(redis.opsForValue().get(OLDEST_VERSION_KEY), OLDEST_VERSION_KEY);
            Object published = redis.opsForHash().get(METADATA_KEY, "publishedAt");
            return new SyncMetadata(current, oldest, parseInstant(published));
        } catch (RedisConnectionFailureException e) {
            throw new CatedraUnavailableException("Redis de la catedra no disponible", 0, null, e);
        }
    }

    @Override
    public Optional<VersionChanges> readChanges(long version) {
        try {
            String raw = templateSupplier.get().opsForValue().get(CHANGES_KEY_PREFIX + version);
            return raw == null ? Optional.empty() : Optional.of(parseChanges(raw, version));
        } catch (RedisConnectionFailureException e) {
            throw new CatedraUnavailableException("Redis de la catedra no disponible", 0, null, e);
        }
    }

    private VersionChanges parseChanges(String raw, long expectedVersion) {
        ChangesJson parsed;
        try {
            parsed = json.readValue(raw, ChangesJson.class);
        } catch (RuntimeException e) {
            throw new CatedraIntegrationException("JSON de cambios invalido para la version " + expectedVersion);
        }
        if (parsed == null || parsed.changes() == null || parsed.version() != expectedVersion) {
            throw new CatedraIntegrationException("Cambios inconsistentes para la version " + expectedVersion);
        }
        ChangeLists lists = parsed.changes();
        return new VersionChanges(expectedVersion, orEmpty(lists.professionalCategories()),
                orEmpty(lists.professionals()), orEmpty(lists.weeklySchedules()));
    }

    private static long requiredLong(String value, String key) {
        if (value == null) {
            throw new CatedraIntegrationException("Redis no tiene la clave " + key);
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new CatedraIntegrationException("Valor no numerico en " + key);
        }
    }

    private static Instant parseInstant(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return Instant.parse(value.toString());
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static List<Long> orEmpty(List<Long> ids) {
        return ids == null ? List.of() : ids;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChangesJson(long version, ChangeLists changes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ChangeLists(List<Long> professionalCategories, List<Long> professionals, List<Long> weeklySchedules) {
    }
}

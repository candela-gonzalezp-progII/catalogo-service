# catalogo-service

Servicio de **catálogo y sincronización** del sistema distribuido de turnos
(Proyecto integrador Programación II — Candela González).

Mantiene una copia local de categorías, profesionales y horarios semanales publicados por la cátedra,
la sincroniza (snapshot completo e incremental) y atiende las búsquedas usando solo datos locales.
Es dueño exclusivo de su base de datos.

## Arquitectura

Hexagonal (puertos y adaptadores). El dominio es Java puro.

```
domain/model            entidades y reglas (sin frameworks)
application/port/in     casos de uso que ofrece el servicio
application/port/out    lo que el servicio necesita de afuera (cátedra, Redis, base de datos)
application/service     implementación de los casos de uso
infrastructure/catedra  cliente REST de la cátedra (JWT técnico, reintentos acotados)
infrastructure/redis    lectura del catálogo publicado en Redis
infrastructure/persistence  JPA + Flyway (Postgres)
infrastructure/web      controllers REST
infrastructure/config   cableado de beans
```

Decisiones en `documentacion-turnos-prog2` (ADR-001).

## Requisitos

- Java 21
- Docker (Postgres local y tests de integración con Testcontainers)
- Acceso a la red ZeroTier de la cátedra para integrar contra el servicio real (no hace falta para correr los tests)

## Configuración

1. Copiar las variables: `cp .env.example .env` y completar los valores.
2. Levantar la base: `docker compose up -d`
3. Ejecutar: `./gradlew bootRun`

El `.env` nunca se sube al repositorio. Las credenciales de la cuenta técnica de la cátedra
solo viven en ese archivo y no se exponen a la app móvil.

## Tests

```
./gradlew test
```

Los tests de integración usan Postgres y Redis reales con Testcontainers (no H2). Requieren Docker en ejecución.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/sync/status` | Estado, versión local y último error de sincronización |

## Estado del proyecto

| Caso de uso | Estado |
|---|---|
| CU-04 Autenticar cuenta técnica | en desarrollo |
| CU-05 Snapshot completo | en desarrollo |
| CU-06 Sincronización incremental | en desarrollo |
| CU-07 Detección de discontinuidad | pendiente |
| CU-08 Búsqueda y filtros | pendiente |
| CU-17 Estado de sincronización | en desarrollo |

Casos de uso completos: `documentacion-turnos-prog2/casos-de-uso`.

# Catálogo Service

Servicio backend (Java + Spring Boot) responsable del catálogo de categorías, profesionales y horarios semanales, y de la sincronización completa e incremental con el servicio central de la cátedra (REST, Redis, Kafka).

Proyecto integrador de Programación II

**Alumno:** Candela González

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
infrastructure/security JWT HS256 y política de acceso
infrastructure/web      controllers REST
infrastructure/config   cableado de beans
```

Decisiones en `documentacion-turnos-prog2` (ADR-001).

## Requisitos

- Java 21
- Docker (Postgres local y tests de integración con Testcontainers)
- Acceso a la red de la cátedra para integrar contra el servicio real (no hace falta para correr los tests)

## Configuración

1. Copiar las variables: `cp .env.example .env` y completar los valores.
   - `CATEDRA_BASE_URL`, `CATEDRA_USERNAME`, `CATEDRA_PASSWORD`: los entrega la cátedra por otro canal.
   - `JWT_SECRET`: mínimo 32 caracteres, **idéntico** al de turnos-service (`openssl rand -base64 48`).
     Si está vacío o es corto, la aplicación no arranca (a propósito).
2. Levantar la base: `docker compose up -d`
3. Ejecutar: `./gradlew bootRun`
   Todo en contenedores: `docker compose --profile app up -d --build`.
   `CORS_ALLOWED_ORIGINS`: orígenes web permitidos (vacío = ninguno).

El `.env` nunca se sube al repositorio (repos públicos). Las credenciales de la cuenta técnica de la cátedra
solo viven en ese archivo y no se exponen a la app móvil.

## Seguridad

Todos los endpoints exigen `Authorization: Bearer <jwt>`:

| Ruta | Quién puede |
|---|---|
| `/api/professionals`, `/api/categories` | usuario final (`ROLE_USER`/`ROLE_ADMIN`) o servicio (`ROLE_SERVICE`) |
| `/api/sync/**` | solo servicio (`ROLE_SERVICE`) |

Sin token, firma inválida o token vencido: `401`. Token de usuario en una ruta interna: `403`.
Errores en `application/problem+json` con un `code` estable.

## Tests

```
./gradlew test
```

Los tests de integración usan Postgres y Redis reales con Testcontainers (no H2). Requieren Docker en ejecución.

## Endpoints

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/professionals` | Búsqueda y filtros (texto, categoría, habilitados) con paginación, sobre la copia local |
| GET | `/api/professionals/{id}` | Detalle de un profesional con sus horarios semanales |
| GET | `/api/categories` | Categorías disponibles |
| GET | `/api/sync/status` | Estado, versión local y último error de sincronización (solo servicios) |

## Estado del proyecto

| Caso de uso | Estado |
|---|---|
| CU-04 Autenticar cuenta técnica | implementado |
| CU-05 Snapshot completo | implementado |
| CU-06 Sincronización incremental (Kafka + Redis) | implementado |
| CU-07 Detección de discontinuidad y reconstrucción | implementado |
| CU-08 Búsqueda y filtros | implementado |
| CU-17 Estado de sincronización | implementado |
| CU-18 Seguridad entre servicios | implementado |

Casos de uso completos: `documentacion-turnos-prog2/casos-de-uso`.

-- Los IDs son los que informa el servicio central de la catedra: no se autogeneran.

CREATE TABLE professional_category (
    id          BIGINT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE professional (
    id          BIGINT PRIMARY KEY,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES professional_category (id),
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_professional_category_id ON professional (category_id);

CREATE TABLE weekly_schedule (
    id              BIGINT PRIMARY KEY,
    professional_id BIGINT NOT NULL REFERENCES professional (id),
    day_of_week     VARCHAR(20) NOT NULL,
    start_time      TIME NOT NULL,
    end_time        TIME NOT NULL
);

CREATE INDEX idx_weekly_schedule_professional_id ON weekly_schedule (professional_id);

CREATE TABLE sync_state (
    id                     BIGINT PRIMARY KEY,
    last_snapshot_version  BIGINT,
    last_synced_at         TIMESTAMP,
    status                 VARCHAR(30) NOT NULL
);

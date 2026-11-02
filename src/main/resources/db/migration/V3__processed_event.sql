-- CU-06: registro de eventIds de Kafka ya procesados (idempotencia: la entrega es "al menos una vez").
CREATE TABLE processed_event (
    event_id     VARCHAR(200) PRIMARY KEY,
    processed_at TIMESTAMP NOT NULL
);

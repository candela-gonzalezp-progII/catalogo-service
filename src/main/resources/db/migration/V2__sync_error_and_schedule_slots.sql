-- CU-05 / CU-17: el contrato de la catedra trae la duracion del turno y el estado habilitado
-- de cada horario, y el servicio debe informar el ultimo error de sincronizacion.

ALTER TABLE weekly_schedule ADD COLUMN slot_duration_minutes INTEGER NOT NULL DEFAULT 30;
ALTER TABLE weekly_schedule ALTER COLUMN slot_duration_minutes DROP DEFAULT;
ALTER TABLE weekly_schedule ADD COLUMN active BOOLEAN NOT NULL DEFAULT TRUE;

ALTER TABLE sync_state ADD COLUMN last_error VARCHAR(500);

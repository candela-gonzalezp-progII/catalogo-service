package com.integrador.catalogo.domain.model;

/** Resultado de procesar una notificacion de catalogo. */
public enum SyncOutcome {
    /** El eventId ya se habia procesado: no se hizo nada. */
    IGNORED_DUPLICATE,
    /** La version local ya era igual o posterior. */
    ALREADY_UP_TO_DATE,
    /** Se aplicaron una o mas versiones incrementales. */
    INCREMENTAL_APPLIED,
    /** No se pudo continuar de forma segura y se reconstruyo desde un snapshot. */
    SNAPSHOT_APPLIED
}

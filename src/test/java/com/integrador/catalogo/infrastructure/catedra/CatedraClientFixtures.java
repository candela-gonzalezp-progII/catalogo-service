package com.integrador.catalogo.infrastructure.catedra;

import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/** Armado comun de los tests del cliente de la catedra: un servidor simulado, sin red real. */
final class CatedraClientFixtures {

    static final String BASE = "http://catedra.test";

    static final String AUTH_JSON = """
            {"id_token":"jwt-tecnico","integration":{
              "groupId":"proyecto-candela","redisHost":"redis.catedra.internal","redisPort":6379,
              "redisUsername":"grp_proyecto-candela","redisPassword":"clave-redis",
              "redisReadNamespace":"catedra:sync:*","redisWriteNamespace":"alumnos:proyecto-candela:*",
              "kafkaBootstrapServers":"kafka.catedra.internal:9092","kafkaConsumerGroupId":"alumnos-proyecto-candela",
              "kafkaCatalogTopic":"catedra.catalog.proyecto-candela",
              "kafkaAppointmentActionsTopic":"alumnos.turnos.acciones.proyecto-candela",
              "kafkaAppointmentPhoneTopic":"catedra.turnos.telefono.proyecto-candela",
              "provisioningStatus":"PROVISIONED","campoNuevo":"se ignora"}}
            """;

    final MockRestServiceServer server;
    final CatedraTokenProvider tokens;
    final CatedraApiClient api;

    CatedraClientFixtures() {
        RestClient.Builder builder = RestClient.builder().baseUrl(BASE);
        this.server = MockRestServiceServer.bindTo(builder).build();
        RestClient client = builder.build();
        RetryExecutor retry = new RetryExecutor(3, Duration.ZERO);
        CatedraProperties properties = new CatedraProperties(BASE, "candela", "secreta",
                Duration.ofSeconds(1), Duration.ofSeconds(1), 3, Duration.ZERO, false);
        this.tokens = new CatedraTokenProvider(client, properties, retry);
        this.api = new CatedraApiClient(client, tokens, retry);
    }
}

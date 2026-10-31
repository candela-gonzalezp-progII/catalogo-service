package com.integrador.catalogo.infrastructure.catedra.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.integrador.catalogo.application.exception.CatedraIntegrationException;
import com.integrador.catalogo.domain.model.IntegrationConfig;
import com.integrador.catalogo.domain.model.ProvisioningStatus;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IntegrationItem(
        String groupId,
        String redisHost,
        Integer redisPort,
        String redisUsername,
        String redisPassword,
        String redisReadNamespace,
        String redisWriteNamespace,
        String kafkaBootstrapServers,
        String kafkaConsumerGroupId,
        String kafkaCatalogTopic,
        String kafkaAppointmentActionsTopic,
        String kafkaAppointmentPhoneTopic,
        String provisioningStatus) {

    public IntegrationConfig toDomain() {
        return new IntegrationConfig(groupId, redisHost, redisPort, redisUsername, redisPassword,
                redisReadNamespace, redisWriteNamespace, kafkaBootstrapServers, kafkaConsumerGroupId,
                kafkaCatalogTopic, kafkaAppointmentActionsTopic, kafkaAppointmentPhoneTopic, parseStatus());
    }

    private ProvisioningStatus parseStatus() {
        try {
            return ProvisioningStatus.valueOf(provisioningStatus);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CatedraIntegrationException("provisioningStatus desconocido: " + provisioningStatus);
        }
    }

    @Override
    public String toString() {
        return "IntegrationItem[groupId=" + groupId + ", provisioningStatus=" + provisioningStatus + ", secretos=***]";
    }
}

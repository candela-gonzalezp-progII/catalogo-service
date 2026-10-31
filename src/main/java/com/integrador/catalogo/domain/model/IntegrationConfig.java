package com.integrador.catalogo.domain.model;

/**
 * Configuracion de integracion asignada por la catedra a la cuenta tecnica.
 * Contiene un secreto (redisPassword): toString() lo oculta para que nunca llegue a un log.
 */
public record IntegrationConfig(
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
        ProvisioningStatus provisioningStatus) {

    public boolean isProvisioned() {
        return provisioningStatus == ProvisioningStatus.PROVISIONED;
    }

    @Override
    public String toString() {
        return "IntegrationConfig[groupId=" + groupId
                + ", provisioningStatus=" + provisioningStatus
                + ", redisHost=" + redisHost + ", redisPort=" + redisPort
                + ", redisPassword=***]";
    }
}

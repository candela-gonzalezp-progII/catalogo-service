package com.integrador.catalogo.infrastructure.kafka;

import com.integrador.catalogo.application.port.in.HandleCatalogUpdateUseCase;
import com.integrador.catalogo.application.port.out.CatedraAuthPort;
import com.integrador.catalogo.domain.model.IntegrationConfig;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Arranca el consumer de CatalogUpdated con los datos que entrega la catedra en
 * /api/student/integration (bootstrap, consumer group y topic). Se activa con CATEDRA_KAFKA_ENABLED=true.
 * Si la integracion no esta PROVISIONED o la catedra no responde, la app igual arranca.
 */
@Component
@ConditionalOnProperty(name = "catedra.kafka-enabled", havingValue = "true")
public class CatalogKafkaStarter implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CatalogKafkaStarter.class);

    private final CatedraAuthPort authPort;
    private final HandleCatalogUpdateUseCase useCase;
    private CatalogUpdatedConsumer consumer;
    private Thread thread;

    public CatalogKafkaStarter(CatedraAuthPort authPort, HandleCatalogUpdateUseCase useCase) {
        this.authPort = authPort;
        this.useCase = useCase;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            IntegrationConfig config = authPort.fetchIntegration();
            if (!config.isProvisioned()) {
                log.warn("Integracion no PROVISIONED ({}): no se inicia el consumer de Kafka", config.provisioningStatus());
                return;
            }
            consumer = new CatalogUpdatedConsumer(new KafkaConsumer<>(properties(config)), config.kafkaCatalogTopic(), useCase);
            thread = new Thread(consumer, "catalog-updated-consumer");
            thread.setDaemon(true);
            thread.start();
            log.info("Consumer de catalogo iniciado en el topic {}", config.kafkaCatalogTopic());
        } catch (RuntimeException e) {
            log.warn("No se pudo iniciar el consumer de Kafka: {}", e.getMessage());
        }
    }

    private static Properties properties(IntegrationConfig config) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.kafkaBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, config.kafkaConsumerGroupId());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "15000");
        return props;
    }

    @PreDestroy
    void stop() {
        if (consumer != null) {
            consumer.stop();
        }
    }
}

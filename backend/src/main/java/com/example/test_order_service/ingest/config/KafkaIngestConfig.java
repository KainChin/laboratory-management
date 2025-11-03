package com.example.test_order_service.ingest.config;

import com.example.test_order_service.ingest.dto.TestResultIngestFullPayload;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaIngestConfig {
    private static final Logger log = LoggerFactory.getLogger(KafkaIngestConfig.class);
    private final KafkaProperties kafkaProperties;

    public KafkaIngestConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ConsumerFactory<String, TestResultIngestFullPayload> ingestConsumerFactory() {
        Map<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
        log.info("Effective bootstrap servers: {}", kafkaProperties.getBootstrapServers());
        log.info("Effective consumer.auto-offset-reset: {}", props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));

        // Cấu hình Deserializer hoàn toàn bằng code
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        JsonDeserializer<TestResultIngestFullPayload> valueDeserializer =
                new JsonDeserializer<>(TestResultIngestFullPayload.class, false); // useTypeHeaders = false
        valueDeserializer.addTrustedPackages(
                "com.example.test_order_service",
                "com.example.test_order_service.ingest"
        );
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TestResultIngestFullPayload> kafkaIngestListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TestResultIngestFullPayload> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ingestConsumerFactory());
        factory.setCommonErrorHandler(new DefaultErrorHandler((record, ex) -> {
            log.error("Ingest record failed after retries, record={}", record, ex);
        }, new FixedBackOff(1000L, 3)));
        factory.setConcurrency(2);
        return factory;
    }
}
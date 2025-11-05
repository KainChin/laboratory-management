package com.example.test_order_service.ingest.config;

import com.example.test_order_service.ingest.dto.InstrumentResultPayload;
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

import java.util.Map;

@Configuration
public class KafkaIngestConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaIngestConfig.class);

    /**
     * Cấu hình ConsumerFactory để nhận các đối tượng Java từ message JSON.
     * @param kafkaProperties Spring Boot tự động inject bean này từ application.yml.
     * @return ConsumerFactory được cấu hình.
     */
    @Bean
    public ConsumerFactory<String, InstrumentResultPayload> ingestConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);

        // Cấu hình JsonDeserializer cho DTO InstrumentResultPayload
        JsonDeserializer<InstrumentResultPayload> valueDeserializer = new JsonDeserializer<>(InstrumentResultPayload.class, false);
        // Thêm package chứa DTO vào danh sách tin cậy để tránh lỗi deserialization
        valueDeserializer.addTrustedPackages("com.example.test_order_service.ingest.dto");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    /**
     * Cấu hình ListenerContainerFactory để tạo các Kafka listener.
     * Bao gồm cấu hình xử lý lỗi (retry) và số luồng xử lý đồng thời.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, InstrumentResultPayload> kafkaIngestListenerContainerFactory(
            ConsumerFactory<String, InstrumentResultPayload> ingestConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, InstrumentResultPayload> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ingestConsumerFactory);

        // Cấu hình retry: thử lại 3 lần, mỗi lần cách nhau 1 giây nếu xử lý message lỗi
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, ex) -> log.error("Ingest record failed after all retries. record={}", record, ex),
                new FixedBackOff(1000L, 3L)
        );
        factory.setCommonErrorHandler(errorHandler);

        // Số luồng tiêu thụ message đồng thời từ topic
        factory.setConcurrency(2);
        return factory;
    }
}
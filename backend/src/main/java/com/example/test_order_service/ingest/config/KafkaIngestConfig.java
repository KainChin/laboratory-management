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

import java.util.Map;

@Configuration
public class KafkaIngestConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaIngestConfig.class);

    // Không cần inject KafkaProperties vào constructor nữa, vì cách mới sẽ dùng bean này trực tiếp
    // private final KafkaProperties kafkaProperties;
    // public KafkaIngestConfig(KafkaProperties kafkaProperties) {
    //     this.kafkaProperties = kafkaProperties;
    // }

    /**
     * Cấu hình ConsumerFactory theo cách làm mới của Spring Boot 3.2+.
     * Sử dụng trực tiếp bean KafkaProperties do Spring Boot tự động tạo ra.
     * Cách này an toàn hơn và loại bỏ cảnh báo 'deprecated'.
     */
    @Bean
    public ConsumerFactory<String, TestResultIngestFullPayload> ingestConsumerFactory(KafkaProperties kafkaProperties) {
        // Lấy toàn bộ cấu hình consumer từ application.yml một cách an toàn
        Map<String, Object> props = kafkaProperties.buildConsumerProperties(null);

        log.info("Effective bootstrap servers: {}", props.get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        log.info("Effective consumer.auto-offset-reset: {}", props.get(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG));

        // Cấu hình Value Deserializer (Key Deserializer đã được tự động cấu hình từ application.yml)
        JsonDeserializer<TestResultIngestFullPayload> valueDeserializer = new JsonDeserializer<>(TestResultIngestFullPayload.class, false);
        valueDeserializer.addTrustedPackages(
                "com.example.test_order_service.ingest.dto",
                "com.example.test_order_service.ingest"
                // Thêm package gốc để an toàn hơn
        );

        // Tạo factory với các thuộc tính đã có và Deserializer đã được tùy chỉnh
        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TestResultIngestFullPayload> kafkaIngestListenerContainerFactory(
            ConsumerFactory<String, TestResultIngestFullPayload> ingestConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, TestResultIngestFullPayload> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(ingestConsumerFactory);

        // Cấu hình Error Handler: thử lại 3 lần, mỗi lần cách nhau 1 giây
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, ex) -> log.error("Ingest record failed after retries, record={}", record, ex),
                new FixedBackOff(1000L, 3L) // 1000ms interval, 3 max retries
        );
        factory.setCommonErrorHandler(errorHandler);

        factory.setConcurrency(2); // Số luồng xử lý đồng thời
        return factory;
    }
}
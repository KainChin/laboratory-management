package com.example.test_order_service.ingest.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaProducerConfigTest {

    private final KafkaProducerConfig config = new KafkaProducerConfig();

    @Test
    void testProducerFactory() {
        KafkaProperties props = new KafkaProperties();
        ProducerFactory<String, Object> factory = config.producerFactory(props);
        assertNotNull(factory);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testKafkaTemplate() {
        ProducerFactory<String, Object> mockFactory = mock(ProducerFactory.class);
        KafkaTemplate<String, Object> template = config.kafkaTemplate(mockFactory);
        assertNotNull(template);
        assertEquals(mockFactory, template.getProducerFactory());
    }
}

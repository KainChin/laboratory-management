package com.example.test_order_service.ingest.config;

import com.example.test_order_service.ingest.dto.InstrumentResultPayload;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KafkaIngestConfigTest {

    private final KafkaIngestConfig config = new KafkaIngestConfig();

    @Test
    void testIngestConsumerFactory() {
        KafkaProperties props = new KafkaProperties();
        ConsumerFactory<String, InstrumentResultPayload> factory = config.ingestConsumerFactory(props);
        assertNotNull(factory);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testKafkaIngestListenerContainerFactory() {
        ConsumerFactory<String, InstrumentResultPayload> mockFactory = mock(ConsumerFactory.class);
        ConcurrentKafkaListenerContainerFactory<String, InstrumentResultPayload> containerFactory =
                config.kafkaIngestListenerContainerFactory(mockFactory);

        assertNotNull(containerFactory);
        assertEquals(mockFactory, containerFactory.getConsumerFactory());
    }
}

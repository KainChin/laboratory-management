package com.example.test_order_service.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.core.env.Environment;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StartupConfigLoggerTest {

    @Test
    void logConfig_shouldLogEnvironmentAndKafkaProperties() {
        // mocks
        Environment env = mock(Environment.class);
        KafkaProperties kafkaProperties = mock(KafkaProperties.class);
        KafkaProperties.Consumer consumerProps = mock(KafkaProperties.Consumer.class);

        when(env.getProperty("spring.kafka.bootstrap-servers"))
                .thenReturn("localhost:9092");
        when(kafkaProperties.getBootstrapServers())
                .thenReturn(List.of("localhost:9092"));
        when(kafkaProperties.getConsumer())
                .thenReturn(consumerProps);
        when(consumerProps.getAutoOffsetReset())
                .thenReturn("earliest");

        StartupConfigLogger loggerBean = new StartupConfigLogger(env, kafkaProperties);

        // attach appender to capture logs
        Logger logbackLogger = (Logger) LoggerFactory.getLogger(StartupConfigLogger.class);
        ListAppender<ch.qos.logback.classic.spi.ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        logbackLogger.addAppender(appender);

        // act
        loggerBean.logConfig();

        // verify interactions
        verify(env, times(1)).getProperty("spring.kafka.bootstrap-servers");
        verify(kafkaProperties, times(1)).getBootstrapServers();
        verify(kafkaProperties, times(1)).getConsumer();
        verify(consumerProps, times(1)).getAutoOffsetReset();

        // verify log contents
        var logs = appender.list;
        assertEquals(3, logs.size(), "Should log exactly 3 lines");

        assertTrue(logs.get(0).getFormattedMessage()
                .contains("ENV spring.kafka.bootstrap-servers = localhost:9092"));
        assertTrue(logs.get(1).getFormattedMessage()
                .contains("KafkaProperties bootstrapServers = [localhost:9092]"));
        assertTrue(logs.get(2).getFormattedMessage()
                .contains("KafkaProperties consumer.auto-offset-reset = earliest"));

        // verify log level
        assertEquals(Level.INFO, logs.get(0).getLevel());
        assertEquals(Level.INFO, logs.get(1).getLevel());
        assertEquals(Level.INFO, logs.get(2).getLevel());

        // cleanup
        logbackLogger.detachAppender(appender);
    }
}

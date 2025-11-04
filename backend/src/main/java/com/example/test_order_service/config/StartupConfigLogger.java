package com.example.test_order_service.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
public class StartupConfigLogger {
    private static final Logger log = LoggerFactory.getLogger(StartupConfigLogger.class);

    private final Environment env;
    private final KafkaProperties kafkaProperties;

    public StartupConfigLogger(Environment env, KafkaProperties kafkaProperties) {
        this.env = env;
        this.kafkaProperties = kafkaProperties;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logConfig() {
        log.info("ENV spring.kafka.bootstrap-servers = {}", env.getProperty("spring.kafka.bootstrap-servers"));
        log.info("KafkaProperties bootstrapServers = {}", kafkaProperties.getBootstrapServers());
        log.info("KafkaProperties consumer.auto-offset-reset = {}", kafkaProperties.getConsumer().getAutoOffsetReset());
    }
}
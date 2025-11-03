package com.example.test_order_service.event.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration cho messaging với RabbitMQ
 * 
 * CHỈ được kích hoạt khi: app.events.enabled = true
 * Nếu false hoặc không set → Config này KHÔNG được tạo
 * → Application vẫn chạy bình thường mà không cần RabbitMQ
 */
@Configuration
@ConditionalOnProperty(name = "app.events.enabled", havingValue = "true")
@Slf4j
public class MessagingConfig {
    
    /**
     * Topic exchange cho test order events
     */
    @Bean
    public TopicExchange testOrderExchange() {
        log.info("📦 Creating test-order-exchange...");
        return new TopicExchange("test-order-exchange");
    }
    
    /**
     * Queue cho Monitoring Service
     */
    @Bean
    public Queue monitoringQueue() {
        log.info("📬 Creating monitoring-service-queue...");
        return QueueBuilder
                .durable("monitoring-service-queue")
                .build();
    }
    
    /**
     * Binding: Listen tất cả test.order.* events
     */
    @Bean
    public Binding monitoringBinding() {
        log.info("🔗 Binding monitoring queue to exchange...");
        return BindingBuilder
                .bind(monitoringQueue())
                .to(testOrderExchange())
                .with("test.order.*");  // Pattern: test.order.created, test.order.status.changed, etc.
    }
}


package com.example.test_order_service.event.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class MessagingConfigTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(AutoConfigurations.of(MessagingConfig.class));

    @Test
    void whenEventsEnabled_true_shouldCreateAllBeans() {
        contextRunner
                .withPropertyValues("app.events.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(TopicExchange.class);
                    assertThat(context).hasSingleBean(Queue.class);
                    assertThat(context).hasSingleBean(Binding.class);

                    TopicExchange exchange = context.getBean(TopicExchange.class);
                    assertThat(exchange.getName()).isEqualTo("test-order-exchange");

                    Queue queue = context.getBean(Queue.class);
                    assertThat(queue.getName()).isEqualTo("monitoring-service-queue");
                    assertThat(queue.isDurable()).isTrue();

                    Binding binding = context.getBean(Binding.class);
                    assertThat(binding.getDestination()).isEqualTo("monitoring-service-queue");
                    assertThat(binding.getExchange()).isEqualTo("test-order-exchange");
                    assertThat(binding.getRoutingKey()).isEqualTo("test.order.*");
                });
    }

    @Test
    void whenEventsEnabled_false_shouldNotCreateBeans() {
        contextRunner
                .withPropertyValues("app.events.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(TopicExchange.class);
                    assertThat(context).doesNotHaveBean(Queue.class);
                    assertThat(context).doesNotHaveBean(Binding.class);
                });
    }

    @Test
    void whenEventsEnabled_missing_shouldNotCreateBeans() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(TopicExchange.class);
            assertThat(context).doesNotHaveBean(Queue.class);
            assertThat(context).doesNotHaveBean(Binding.class);
        });
    }
}

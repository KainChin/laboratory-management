package com.example.test_order_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import static org.junit.jupiter.api.Assertions.*;

class SchedulingConfigTest {

    @Test
    void testTaskScheduler() {
        SchedulingConfig config = new SchedulingConfig();
        TaskScheduler scheduler = config.taskScheduler();

        assertNotNull(scheduler);
        assertTrue(scheduler instanceof ThreadPoolTaskScheduler);

        ThreadPoolTaskScheduler threadPoolScheduler = (ThreadPoolTaskScheduler) scheduler;
        assertEquals("instrument-poll-", threadPoolScheduler.getThreadNamePrefix());

        threadPoolScheduler.destroy();
    }
}

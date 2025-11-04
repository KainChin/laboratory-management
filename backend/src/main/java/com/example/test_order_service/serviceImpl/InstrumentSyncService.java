package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.integration.instrument.InstrumentClient;
import com.example.test_order_service.repository.TestOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstrumentSyncService {

    private final InstrumentClient instrumentClient;
    private final TestOrderRepository testOrderRepository;
    private final TaskScheduler taskScheduler;

    @Value("${wait.poll-interval-ms:5000}")
    private long pollIntervalMs;

    @Value("${wait.max-wait-ms:600000}")
    private long maxWaitMs;

    public void requestOrWait(String orderId) {
        try {
            log.info("Requesting instrument results for order {}", orderId);
            String payload = instrumentClient.requestResults(orderId);
            if (payload != null && !payload.isEmpty()) {
                log.info("Received instrument results for order {} ({} bytes)", orderId, payload.length());
                updateStatusIfPresent(orderId, TestOrderStatus.COMPLETED);
                return;
            }
            // Treat empty payload as waiting
            markWaiting(orderId);
            schedulePoll(orderId);
        } catch (Exception e) {
            log.warn("Instrument request failed/timeout for order {}: {}", orderId, e.getMessage());
            markWaiting(orderId);
            schedulePoll(orderId);
        }
    }

    private void markWaiting(String orderId) {
        updateStatusIfPresent(orderId, TestOrderStatus.PENDING);
    }

    private void updateStatusIfPresent(String orderId, TestOrderStatus status) {
        Optional<TestOrder> opt = testOrderRepository.findById(orderId);
        if (opt.isPresent()) {
            TestOrder order = opt.get();
            order.setStatus(status);
            testOrderRepository.save(order);
        }
    }

    private void schedulePoll(String orderId) {
        long deadline = System.currentTimeMillis() + maxWaitMs;
        ScheduledFuture<?>[] ref = new ScheduledFuture<?>[1];
        ref[0] = taskScheduler.scheduleAtFixedRate(() -> {
            if (System.currentTimeMillis() > deadline) {
                log.warn("Stop polling instrument for order {} due to timeout window exceeded", orderId);
                if (ref[0] != null) ref[0].cancel(false);
                return;
            }
            try {
                String payload = instrumentClient.requestResults(orderId);
                if (payload != null && !payload.isEmpty()) {
                    log.info("Instrument results available for order {} during polling", orderId);
                    updateStatusIfPresent(orderId, TestOrderStatus.COMPLETED);
                    if (ref[0] != null) ref[0].cancel(false);
                } else {
                    log.info("Still waiting for instrument results for order {}", orderId);
                }
            } catch (Exception ex) {
                log.debug("Polling attempt failed for order {}: {}", orderId, ex.getMessage());
            }
        }, Instant.now().plusMillis(pollIntervalMs), Duration.ofMillis(pollIntervalMs));
    }
}



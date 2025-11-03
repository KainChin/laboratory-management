package com.example.test_order_service.integration.instrument;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class InstrumentClient {

    private final WebClient webClient;
    private final Duration timeout;
    private final int maxAttempts;
    private final Duration backoff;

    public InstrumentClient(
            WebClient.Builder builder,
            @Value("${instrument.base-url:http://localhost:8081}") String baseUrl,
            @Value("${instrument.timeout-ms:3000}") long timeoutMs,
            @Value("${instrument.retry.max-attempts:3}") int maxAttempts,
            @Value("${instrument.retry.backoff-ms:500}") long backoffMs
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.timeout = Duration.ofMillis(timeoutMs);
        this.maxAttempts = Math.max(1, maxAttempts);
        this.backoff = Duration.ofMillis(Math.max(0, backoffMs));
    }

    public String requestResults(String orderId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/instruments/results")
                        .queryParam("orderId", orderId)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(timeout)
                .retryWhen(Retry.backoff(Math.max(0, maxAttempts - 1), backoff))
                .block(timeout);
    }
}



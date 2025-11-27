package com.example.test_order_service.ingest.publisher;

import com.example.test_order_service.ingest.dto.ResyncRequestPayload;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResyncRequestPublisherTest {

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    ResyncRequestPublisher publisher;

    @Test
    void requestResync_nullOrderId_shouldNotSend() {
        publisher.requestResync(null, "BCT-1", "reason");
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void requestResync_blankOrderId_shouldNotSend() {
        publisher.requestResync("   ", "BCT-1", "reason");
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void requestResync_valid_shouldBuildPayloadAndSend() {
        // stub send future chỉ trong test này
        RecordMetadata meta = mock(RecordMetadata.class);
        when(meta.topic()).thenReturn("test-result-resync-request-topic");
        when(meta.partition()).thenReturn(1);

        SendResult<String, Object> sendResult = mock(SendResult.class);
        when(sendResult.getRecordMetadata()).thenReturn(meta);

        CompletableFuture<SendResult<String, Object>> okFuture =
                CompletableFuture.completedFuture(sendResult);

        when(kafkaTemplate.send(eq("test-result-resync-request-topic"), eq("TO-1"), any(ResyncRequestPayload.class)))
                .thenReturn(okFuture);

        publisher.requestResync("TO-1", "BCT-999", "ManualTrigger");

        ArgumentCaptor<ResyncRequestPayload> captor =
                ArgumentCaptor.forClass(ResyncRequestPayload.class);

        verify(kafkaTemplate, times(1))
                .send(eq("test-result-resync-request-topic"), eq("TO-1"), captor.capture());

        ResyncRequestPayload payload = captor.getValue();
        assertNotNull(payload);
        assertEquals("TO-1", payload.getTestOrderId());
        assertEquals("BCT-999", payload.getBloodCollectionId());
        assertEquals("ManualTrigger", payload.getReason());
    }

    @Test
    void requestResync_sendThrowsException_shouldNotCrash() {
        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("kafka fail"));

        assertDoesNotThrow(() ->
                publisher.requestResync("TO-X", "BCT-X", "reason"));

        verify(kafkaTemplate, times(1))
                .send(eq("test-result-resync-request-topic"), eq("TO-X"), any(ResyncRequestPayload.class));
    }
}

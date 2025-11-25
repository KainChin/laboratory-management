package com.example.test_order_service.ingest.publisher;

import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.ingest.dto.CommentEventPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private CommentEventPublisher publisher;

    private CompletableFuture<SendResult<String, Object>> future;

    @BeforeEach
    void setup() {
        future = new CompletableFuture<>();
    }

    @Test
    void publishCommentEvent_nullComment_currentImplThrowsNpe() {
        assertThrows(NullPointerException.class,
                () -> publisher.publishCommentEvent(null, "COMMENT_CREATED"));
        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void publishCommentEvent_nullTestOrder_shouldNotSend() {
        Comment comment = mock(Comment.class);
        when(comment.getTestOrder()).thenReturn(null);

        publisher.publishCommentEvent(comment, "COMMENT_CREATED");

        verifyNoInteractions(kafkaTemplate);
    }

    @Test
    void publishCommentEvent_validComment_shouldBuildPayloadAndSend() {
        TestOrder testOrder = mock(TestOrder.class);
        when(testOrder.getTestOrderId()).thenReturn("TO-1");
        when(testOrder.getEmail()).thenReturn("a@gmail.com");

        Comment comment = mock(Comment.class);
        when(comment.getTestOrder()).thenReturn(testOrder);
        when(comment.getCommentId()).thenReturn("C-1");
        when(comment.getCommentText()).thenReturn("hello");
        when(comment.getCreatedBy()).thenReturn("user-1");
        when(comment.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(kafkaTemplate.send(eq("comment-events-topic"), eq("TO-1"), any(CommentEventPayload.class)))
                .thenReturn(future);

        publisher.publishCommentEvent(comment, "COMMENT_CREATED");

        ArgumentCaptor<CommentEventPayload> captor =
                ArgumentCaptor.forClass(CommentEventPayload.class);

        verify(kafkaTemplate, times(1))
                .send(eq("comment-events-topic"), eq("TO-1"), captor.capture());

        CommentEventPayload payload = captor.getValue();
        assertNotNull(payload);
        assertEquals("COMMENT_CREATED", payload.getEventType());
        assertEquals("C-1", payload.getCommentId());
        assertEquals("TO-1", payload.getTestOrderId());
        assertEquals("a@gmail.com", payload.getEmail());
        assertEquals("hello", payload.getCommentText());
        assertEquals("user-1", payload.getCreatedBy());
        assertNotNull(payload.getCreatedAt());
    }

    @Test
    void publishCommentEvent_sendThrowsException_shouldNotCrash() {
        TestOrder testOrder = mock(TestOrder.class);
        when(testOrder.getTestOrderId()).thenReturn("TO-1");
        when(testOrder.getEmail()).thenReturn("a@gmail.com");

        Comment comment = mock(Comment.class);
        when(comment.getTestOrder()).thenReturn(testOrder);
        when(comment.getCommentId()).thenReturn("C-1");
        when(comment.getCommentText()).thenReturn("hello");
        when(comment.getCreatedBy()).thenReturn("user-1");
        when(comment.getCreatedAt()).thenReturn(LocalDateTime.now());

        when(kafkaTemplate.send(anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("boom"));

        assertDoesNotThrow(() -> publisher.publishCommentEvent(comment, "COMMENT_CREATED"));
    }
}

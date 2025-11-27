package com.example.test_order_service.dto.response;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentResponseTest {

    @Test
    void builderAndGetters_shouldWork() {
        LocalDateTime now = LocalDateTime.now();
        CommentResponse res = CommentResponse.builder()
                .commentId("c1")
                .commentText("hello")
                .createdBy("userA")
                .createdAt(now)
                .build();

        assertEquals("c1", res.getCommentId());
        assertEquals("hello", res.getCommentText());
        assertEquals("userA", res.getCreatedBy());
        assertEquals(now, res.getCreatedAt());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        CommentResponse a = new CommentResponse("c1", "t", "u", LocalDateTime.of(2025,1,1,0,0));
        CommentResponse b = new CommentResponse("c1", "t", "u", LocalDateTime.of(2025,1,1,0,0));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setCommentText("new");
        assertEquals("new", a.getCommentText());

        CommentResponse empty = new CommentResponse();
        empty.setCommentId("x");
        assertEquals("x", empty.getCommentId());
    }
}

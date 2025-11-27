package com.example.test_order_service.entity;

import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class CommentTest {

    static class ExposedComment extends Comment {
        void triggerCreate() { super.onCreate(); }
        void triggerUpdate() { super.onUpdate(); }
    }

    private TestOrder validTestOrder() {
        TestOrder order = new TestOrder();
        order.setTestOrderId("to1");
        order.setPatientName("A");          // nullable=false
        order.setCitizenId("123");          // nullable=false
        order.setCountry("VN");             // nullable=false
        order.setBloodCollectionId("bc1");  // nullable=false
        order.setStatus(TestOrderStatus.PENDING); // nullable=false
        return order;
    }

    @Test
    void onCreate_shouldSetCreatedAt() {
        ExposedComment c = new ExposedComment();
        assertNull(c.getCreatedAt());

        c.triggerCreate();

        assertNotNull(c.getCreatedAt());
    }

    @Test
    void onUpdate_shouldSetUpdatedAt() throws InterruptedException {
        ExposedComment c = new ExposedComment();
        c.triggerCreate();
        LocalDateTime createdAt = c.getCreatedAt();

        Thread.sleep(2);
        c.triggerUpdate();

        assertNotNull(c.getUpdatedAt());
        assertTrue(c.getUpdatedAt().isAfter(createdAt) || c.getUpdatedAt().isEqual(createdAt));
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        TestOrder order = validTestOrder();
        LocalDateTime now = LocalDateTime.now();

        Comment a = new Comment();
        a.setCommentId("c1");
        a.setTestOrder(order);          // nullable=false
        a.setCommentText("hi");         // nullable=false
        a.setCreatedBy("u1");           // nullable=false
        a.setCreatedAt(now);            // nullable=false
        a.setUpdatedBy("u2");           // nullable=false
        a.setUpdatedAt(now);

        Comment b = new Comment("c1", order, "hi", "u1", now, "u2", now);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setCommentText("new");
        assertEquals("new", a.getCommentText());
        assertSame(order, a.getTestOrder());
    }
}

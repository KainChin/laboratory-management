package com.example.test_order_service.base;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BaseEntityTest {

    // Dummy concrete class để test BaseEntity
    static class DummyEntity extends BaseEntity {
        void triggerCreate() { onCreate(); }
        void triggerUpdate() { onUpdate(); }
    }

    @Test
    void onCreate_shouldSetCreatedAtAndDeletedFalse() {
        DummyEntity e = new DummyEntity();

        assertNull(e.getCreatedAt());
        e.setDeleted(true); // giả lập bị set sai trước khi persist

        e.triggerCreate();

        assertNotNull(e.getCreatedAt(), "createdAt should be set on create");
        assertFalse(e.isDeleted(), "deleted should be reset to false on create");
        assertNull(e.getUpdatedAt(), "updatedAt should remain null on create");
    }

    @Test
    void onUpdate_shouldSetUpdatedAt() throws InterruptedException {
        DummyEntity e = new DummyEntity();
        e.triggerCreate();
        LocalDateTime createdAt = e.getCreatedAt();

        // ngủ nhẹ để đảm bảo updatedAt sau createdAt (tránh flake)
        Thread.sleep(2);

        e.triggerUpdate();

        assertNotNull(e.getUpdatedAt(), "updatedAt should be set on update");
        assertEquals(createdAt, e.getCreatedAt(), "createdAt should not change on update");
        assertTrue(e.getUpdatedAt().isAfter(createdAt) || e.getUpdatedAt().isEqual(createdAt),
                "updatedAt should be >= createdAt");
    }

    @Test
    void lombokGettersSettersAndEquality_shouldWork() {
        DummyEntity a = new DummyEntity();
        DummyEntity b = new DummyEntity();

        a.setCreatedBy("u1");
        a.setUpdatedBy("u2");
        a.setDeletedBy("u3");
        a.setDeleted(true);

        b.setCreatedBy("u1");
        b.setUpdatedBy("u2");
        b.setDeletedBy("u3");
        b.setDeleted(true);

        assertEquals("u1", a.getCreatedBy());
        assertEquals("u2", a.getUpdatedBy());
        assertEquals("u3", a.getDeletedBy());
        assertTrue(a.isDeleted());

        // Lombok @Data => equals/hashCode/toString
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());
    }
}

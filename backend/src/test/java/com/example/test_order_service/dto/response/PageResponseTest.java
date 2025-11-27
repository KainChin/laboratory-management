package com.example.test_order_service.dto.response;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PageResponseTest {

    @Test
    void defaultItems_shouldBeEmptyList() {
        PageResponse<String> res = new PageResponse<>();
        assertNotNull(res.getItems());
        assertEquals(Collections.emptyList(), res.getItems());
    }

    @Test
    void builderAndGetters_shouldWork() {
        PageResponse<String> res = PageResponse.<String>builder()
                .currentPage(1)
                .totalPages(3)
                .items(List.of("a", "b"))
                .build();

        assertEquals(1, res.getCurrentPage());
        assertEquals(3, res.getTotalPages());
        assertEquals(List.of("a","b"), res.getItems());
    }

    @Test
    void lombokGeneratedMethods_shouldWork() {
        PageResponse<Integer> a = new PageResponse<>(1,2, List.of(1,2));
        PageResponse<Integer> b = new PageResponse<>(1,2, List.of(1,2));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotNull(a.toString());

        a.setCurrentPage(5);
        assertEquals(5, a.getCurrentPage());
    }
}

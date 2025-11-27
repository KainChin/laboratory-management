package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.response.CommentResponse;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommentMapperTest {

    private final CommentMapper mapper = Mappers.getMapper(CommentMapper.class);

    private TestOrder validOrder() {
        TestOrder o = new TestOrder();
        o.setTestOrderId("to1");
        o.setPatientName("A");
        o.setCitizenId("123");
        o.setCountry("VN");
        o.setBloodCollectionId("bc1");
        o.setStatus(TestOrderStatus.PENDING);
        return o;
    }

    @Test
    void toCommentEntity_shouldMapFromCreateRequest() {
        CreateCommentRequest req = CreateCommentRequest.builder()
                .commentText("hello")
                .build();

        Comment entity = mapper.toCommentEntity(req);

        assertNotNull(entity);
        assertEquals("hello", entity.getCommentText());
        // các field khác không có trong request => null
        assertNull(entity.getCommentId());
        assertNull(entity.getCreatedBy());
    }

    @Test
    void toCommentResponse_shouldMapEntityToDto() {
        LocalDateTime now = LocalDateTime.now();
        Comment c = new Comment();
        c.setCommentId("c1");
        c.setCommentText("text");
        c.setCreatedBy("u1");
        c.setCreatedAt(now);
        c.setUpdatedBy("u2");
        c.setUpdatedAt(now);
        c.setTestOrder(validOrder());

        CommentResponse dto = mapper.toCommentResponse(c);

        assertNotNull(dto);
        assertEquals("c1", dto.getCommentId());
        assertEquals("text", dto.getCommentText());
        assertEquals("u1", dto.getCreatedBy());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void toCommentResponses_shouldMapList() {
        Comment c1 = new Comment();
        c1.setCommentId("c1");
        c1.setCommentText("a");
        c1.setCreatedBy("u1");
        c1.setCreatedAt(LocalDateTime.now());
        c1.setUpdatedBy("u1");
        c1.setTestOrder(validOrder());

        Comment c2 = new Comment();
        c2.setCommentId("c2");
        c2.setCommentText("b");
        c2.setCreatedBy("u2");
        c2.setCreatedAt(LocalDateTime.now());
        c2.setUpdatedBy("u2");
        c2.setTestOrder(validOrder());

        List<CommentResponse> dtos = mapper.toCommentResponses(List.of(c1, c2));

        assertEquals(2, dtos.size());
        assertEquals("c1", dtos.get(0).getCommentId());
        assertEquals("c2", dtos.get(1).getCommentId());
    }
}

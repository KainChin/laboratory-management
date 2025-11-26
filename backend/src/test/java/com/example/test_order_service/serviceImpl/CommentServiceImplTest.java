package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import com.example.test_order_service.dto.response.CommentResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.ingest.publisher.CommentEventPublisher;
import com.example.test_order_service.mapper.CommentMapper;
import com.example.test_order_service.repository.CommentRepository;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.utils.GeneralUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock CommentRepository commentRepository;
    @Mock TestOrderRepository testOrderRepository;
    @Mock CommentMapper commentMapper;
    @Mock CommentEventPublisher commentEventPublisher;

    @InjectMocks CommentServiceImpl service;

    private TestOrder validOrder(String id, boolean deleted) {
        TestOrder o = new TestOrder();
        o.setTestOrderId(id);
        o.setPatientName("A");
        o.setCitizenId("123");
        o.setCountry("VN");
        o.setBloodCollectionId("bc1");
        o.setStatus(TestOrderStatus.PENDING);
        o.setDeleted(deleted);
        return o;
    }

    // -------- createComment --------

    @Test
    void createComment_nullRequest_shouldThrow() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> service.createComment("to1", null));
        assertEquals("No comment data provided", ex.getMessage());
        verifyNoInteractions(testOrderRepository, commentRepository, commentMapper, commentEventPublisher);
    }

    @Test
    void createComment_orderNotFound_shouldThrow() {
        when(testOrderRepository.findById("to1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.createComment("to1", new CreateCommentRequest("x")));

        verify(testOrderRepository).findById("to1");
        verifyNoMoreInteractions(testOrderRepository);
        verifyNoInteractions(commentRepository, commentMapper, commentEventPublisher);
    }

    @Test
    void createComment_deletedOrder_shouldThrow() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", true)));

        assertThrows(ResourceNotFoundException.class,
                () -> service.createComment("to1", new CreateCommentRequest("x")));

        verify(testOrderRepository).findById("to1");
        verifyNoInteractions(commentRepository, commentMapper, commentEventPublisher);
    }

    @Test
    void createComment_success_publishOk() {
        TestOrder order = validOrder("to1", false);
        CreateCommentRequest req = new CreateCommentRequest("hello");

        Comment entity = new Comment();
        entity.setCommentText("hello");

        LocalDateTime now = LocalDateTime.now();
        Comment saved = new Comment();
        saved.setCommentId("c1");
        saved.setCommentText("hello");
        saved.setCreatedBy("user1");
        saved.setCreatedAt(now);

        CommentResponse dto = CommentResponse.builder()
                .commentId("c1")
                .commentText("hello")
                .createdBy("user1")
                .createdAt(now)
                .build();

        when(testOrderRepository.findById("to1")).thenReturn(Optional.of(order));
        when(commentMapper.toCommentEntity(req)).thenReturn(entity);
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);
        when(commentMapper.toCommentResponse(saved)).thenReturn(dto);

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(GeneralUtils::getCurrentUsername).thenReturn("user1");

            RestResponse<CommentResponse> res = service.createComment("to1", req);

            assertNotNull(res);
            assertEquals(200, res.getStatusCode());
            assertEquals("Comment created successfully", res.getMessage());
            assertNotNull(res.getResult());
            assertEquals("c1", res.getResult().getCommentId());

            verify(commentEventPublisher).publishCommentEvent(saved, "COMMENT_CREATED");
        }

        verify(testOrderRepository).findById("to1");
        verify(commentMapper).toCommentEntity(req);
        verify(commentRepository).save(any(Comment.class));
        verify(commentMapper).toCommentResponse(saved);
    }

    @Test
    void createComment_publishThrows_shouldStillReturnSuccess() {
        TestOrder order = validOrder("to1", false);
        CreateCommentRequest req = new CreateCommentRequest("hello");

        Comment entity = new Comment();
        Comment saved = new Comment();
        saved.setCommentId("c1");

        when(testOrderRepository.findById("to1")).thenReturn(Optional.of(order));
        when(commentMapper.toCommentEntity(req)).thenReturn(entity);
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);
        when(commentMapper.toCommentResponse(saved))
                .thenReturn(CommentResponse.builder().commentId("c1").build());

        doThrow(new RuntimeException("kafka down"))
                .when(commentEventPublisher).publishCommentEvent(saved, "COMMENT_CREATED");

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(GeneralUtils::getCurrentUsername).thenReturn("user1");

            RestResponse<CommentResponse> res = service.createComment("to1", req);

            assertNotNull(res);
            assertEquals(200, res.getStatusCode());
            assertNotNull(res.getResult());
            assertEquals("c1", res.getResult().getCommentId());
        }
    }

    // -------- updateComment --------

    @Test
    void updateComment_nullRequest_shouldThrow() {
        IllegalArgumentException ex =
                assertThrows(IllegalArgumentException.class,
                        () -> service.updateComment("to1", "c1", null));
        assertEquals("No comment data provided", ex.getMessage());
        verifyNoInteractions(testOrderRepository, commentRepository, commentMapper, commentEventPublisher);
    }

    @Test
    void updateComment_orderNotFound_shouldThrow() {
        when(testOrderRepository.findById("to1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateComment("to1", "c1", new UpdateCommentRequest("x")));

        verify(testOrderRepository).findById("to1");
        verifyNoInteractions(commentRepository, commentMapper, commentEventPublisher);
    }

    @Test
    void updateComment_deletedOrder_shouldThrow() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", true)));

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateComment("to1", "c1", new UpdateCommentRequest("x")));

        verify(testOrderRepository).findById("to1");
        verifyNoInteractions(commentRepository, commentMapper, commentEventPublisher);
    }

    @Test
    void updateComment_commentNotFound_shouldThrow() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", false)));
        when(commentRepository.findByCommentIdAndTestOrder_TestOrderId("c1", "to1"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateComment("to1", "c1", new UpdateCommentRequest("x")));

        verify(testOrderRepository).findById("to1");
        verify(commentRepository).findByCommentIdAndTestOrder_TestOrderId("c1", "to1");
        verifyNoInteractions(commentMapper, commentEventPublisher);
    }

    @Test
    void updateComment_notOwner_shouldThrow() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", false)));

        Comment c = new Comment();
        c.setCommentId("c1");
        c.setCreatedBy("other");

        when(commentRepository.findByCommentIdAndTestOrder_TestOrderId("c1", "to1"))
                .thenReturn(Optional.of(c));

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(GeneralUtils::getCurrentUsername).thenReturn("user1");

            assertThrows(IllegalStateException.class,
                    () -> service.updateComment("to1", "c1", new UpdateCommentRequest("x")));
        }

        verify(commentRepository, never()).save(any());
        verifyNoInteractions(commentMapper, commentEventPublisher);
    }

    @Test
    void updateComment_success() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", false)));

        Comment c = new Comment();
        c.setCommentId("c1");
        c.setCreatedBy("user1");
        c.setCommentText("old");

        Comment saved = new Comment();
        saved.setCommentId("c1");
        saved.setCommentText("new");
        saved.setCreatedBy("user1");
        saved.setCreatedAt(LocalDateTime.now());

        when(commentRepository.findByCommentIdAndTestOrder_TestOrderId("c1", "to1"))
                .thenReturn(Optional.of(c));
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        CommentResponse outDto = CommentResponse.builder()
                .commentId("c1")
                .commentText("new")
                .build();
        when(commentMapper.toCommentResponse(saved)).thenReturn(outDto);

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(GeneralUtils::getCurrentUsername).thenReturn("user1");

            RestResponse<CommentResponse> res =
                    service.updateComment("to1", "c1", new UpdateCommentRequest("new"));

            assertNotNull(res);
            assertEquals(200, res.getStatusCode());
            assertEquals("Comment updated successfully", res.getMessage());
            assertNotNull(res.getResult());
            assertEquals("c1", res.getResult().getCommentId());

            verify(commentEventPublisher).publishCommentEvent(saved, "COMMENT_UPDATED");
        }

        verify(commentRepository).save(any(Comment.class));
    }

    // -------- deleteComment --------

    @Test
    void deleteComment_orderNotFound_shouldThrow() {
        when(testOrderRepository.findById("to1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteComment("to1", "c1"));

        verify(testOrderRepository).findById("to1");
        verifyNoInteractions(commentRepository, commentMapper, commentEventPublisher);
    }

    @Test
    void deleteComment_deletedOrder_shouldThrow() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", true)));

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteComment("to1", "c1"));

        verify(testOrderRepository).findById("to1");
        verifyNoInteractions(commentRepository, commentMapper, commentEventPublisher);
    }

    @Test
    void deleteComment_commentNotFound_shouldThrow() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", false)));
        when(commentRepository.findByCommentIdAndTestOrder_TestOrderId("c1", "to1"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteComment("to1", "c1"));

        verify(commentRepository).findByCommentIdAndTestOrder_TestOrderId("c1", "to1");
        verifyNoInteractions(commentEventPublisher);
    }

    @Test
    void deleteComment_notOwner_shouldThrow() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", false)));

        Comment c = new Comment();
        c.setCommentId("c1");
        c.setCreatedBy("other");

        when(commentRepository.findByCommentIdAndTestOrder_TestOrderId("c1", "to1"))
                .thenReturn(Optional.of(c));

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(GeneralUtils::getCurrentUsername).thenReturn("user1");

            assertThrows(IllegalStateException.class,
                    () -> service.deleteComment("to1", "c1"));
        }

        verify(commentRepository, never()).deleteById(anyString());
        verifyNoInteractions(commentEventPublisher);
    }

    @Test
    void deleteComment_success_publishOk() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", false)));

        Comment c = new Comment();
        c.setCommentId("c1");
        c.setCreatedBy("user1");

        when(commentRepository.findByCommentIdAndTestOrder_TestOrderId("c1", "to1"))
                .thenReturn(Optional.of(c));

        try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
            mocked.when(GeneralUtils::getCurrentUsername).thenReturn("user1");

            RestResponse<Void> res = service.deleteComment("to1", "c1");

            assertNotNull(res);
            assertEquals(200, res.getStatusCode());
            assertNotNull(res.getMessage());

            verify(commentRepository).deleteById("c1");
            verify(commentEventPublisher).publishCommentEvent(c, "COMMENT_DELETED");
        }
    }

    // -------- getAllComments --------

    @Test
    void getAllComments_orderNotFound_shouldThrow() {
        when(testOrderRepository.findById("to1")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getAllComments("to1"));

        verify(testOrderRepository).findById("to1");
        verifyNoInteractions(commentRepository, commentMapper);
    }

    @Test
    void getAllComments_deletedOrder_shouldThrow() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", true)));

        assertThrows(ResourceNotFoundException.class,
                () -> service.getAllComments("to1"));

        verify(testOrderRepository).findById("to1");
        verifyNoInteractions(commentRepository, commentMapper);
    }

    @Test
    void getAllComments_shouldSortByCreatedAtAscAndMap() {
        when(testOrderRepository.findById("to1"))
                .thenReturn(Optional.of(validOrder("to1", false)));

        Comment c1 = new Comment();
        c1.setCommentId("c1");
        c1.setCreatedAt(LocalDateTime.now().minusHours(2));

        Comment c2 = new Comment();
        c2.setCommentId("c2");
        c2.setCreatedAt(LocalDateTime.now().minusHours(1));

        when(commentRepository.findAllByTestOrder_TestOrderId("to1"))
                .thenReturn(List.of(c2, c1)); // đảo thứ tự

        when(commentMapper.toCommentResponse(c1))
                .thenReturn(CommentResponse.builder().commentId("c1").build());
        when(commentMapper.toCommentResponse(c2))
                .thenReturn(CommentResponse.builder().commentId("c2").build());

        List<CommentResponse> res = service.getAllComments("to1");

        assertEquals(List.of("c1", "c2"),
                res.stream().map(CommentResponse::getCommentId).toList());

        verify(commentRepository).findAllByTestOrder_TestOrderId("to1");
        verify(commentMapper).toCommentResponse(c1);
        verify(commentMapper).toCommentResponse(c2);
    }
}

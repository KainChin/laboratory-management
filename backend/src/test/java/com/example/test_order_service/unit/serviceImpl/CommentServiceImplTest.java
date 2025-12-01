package com.example.test_order_service.unit.serviceImpl;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.ingest.publisher.CommentEventPublisher;
import com.example.test_order_service.mapper.CommentMapper;
import com.example.test_order_service.repository.CommentRepository;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.serviceImpl.CommentServiceImpl;
import com.example.test_order_service.utils.GeneralUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for CommentServiceImpl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CommentServiceImpl Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private TestOrderRepository testOrderRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private CommentEventPublisher commentEventPublisher;

    @Mock
    private WebClient webClient;

    @InjectMocks
    private CommentServiceImpl commentService;

    private TestOrder testOrder;
    private Comment comment;
    private CommentResponse commentResponse;
    private CreateCommentRequest createCommentRequest;
    private UpdateCommentRequest updateCommentRequest;

    @BeforeEach
    void setUp() {
        // tránh Lombok builder missing non-nullable fields:
        // dùng object + setter trực tiếp
        testOrder = new TestOrder();
        testOrder.setTestOrderId("TO-001");
        testOrder.setStatus(TestOrderStatus.PENDING);
        testOrder.setDeleted(false);

        comment = new Comment();
        comment.setCommentId("C-001");
        comment.setTestOrder(testOrder);
        comment.setCommentText("This is a test comment");
        comment.setCreatedBy("Doctor A");
        comment.setCreatedAt(LocalDateTime.now());

        commentResponse = CommentResponse.builder()
                .commentId("C-001")
                .commentText("This is a test comment")
                .createdBy("Doctor A")
                .createdAt(LocalDateTime.now())
                .build();

        createCommentRequest = CreateCommentRequest.builder()
                .commentText("This is a test comment")
                .build();

        updateCommentRequest = UpdateCommentRequest.builder()
                .commentText("Updated comment text")
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // CREATE COMMENT TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Create Comment Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateCommentTests {

        @Test
        @Order(1)
        @DisplayName("Should create comment successfully")
        void shouldCreateCommentSuccessfully() {
            String orderId = "TO-001";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
                mocked.when(GeneralUtils::getCurrentUsername).thenReturn("Doctor A");

                RestResponse<CommentResponse> response =
                        commentService.createComment(orderId, createCommentRequest);

                assertThat(response).isNotNull();
                assertThat(response.getStatusCode()).isEqualTo(200);
                assertThat(response.getMessage()).isEqualTo("Comment created successfully");
                assertThat(response.getResult()).isNotNull();
                assertThat(response.getResult().getCommentText()).isEqualTo("This is a test comment");
                assertThat(response.getTimestamp()).isNotNull();
            }

            verify(testOrderRepository).findById(orderId);
            verify(commentMapper).toCommentEntity(createCommentRequest);
            verify(commentRepository).save(any(Comment.class));
            verify(commentMapper).toCommentResponse(comment);
            verify(commentEventPublisher).publishCommentEvent(any(Comment.class), eq("COMMENT_CREATED"));
        }

        @Test
        @Order(2)
        @DisplayName("Should throw IllegalArgumentException when request is null")
        void shouldThrowWhenRequestNull() {
            assertThatThrownBy(() -> commentService.createComment("TO-001", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("No comment data provided");
        }

        @Test
        @Order(3)
        @DisplayName("Should throw ResourceNotFoundException when test order not found")
        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
            String invalidOrderId = "INVALID-ID";
            when(testOrderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.createComment(invalidOrderId, createCommentRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(invalidOrderId);
            verify(commentMapper, never()).toCommentEntity(any());
            verify(commentRepository, never()).save(any());
        }

        @Test
        @Order(4)
        @DisplayName("Should throw ResourceNotFoundException when test order is deleted")
        void shouldThrowWhenOrderDeleted() {
            String orderId = "TO-001";
            testOrder.setDeleted(true);
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> commentService.createComment(orderId, createCommentRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");
        }

        @Test
        @Order(5)
        @DisplayName("Should handle repository exception during create")
        void shouldHandleRepositoryExceptionDuringCreate() {
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class)))
                    .thenThrow(new DataAccessException("Database error") {});

            try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
                mocked.when(GeneralUtils::getCurrentUsername).thenReturn("Doctor A");

                assertThatThrownBy(() -> commentService.createComment(orderId, createCommentRequest))
                        .isInstanceOf(DataAccessException.class);
            }
        }

        @Test
        @Order(6)
        @DisplayName("Should ignore publisher exception")
        void shouldIgnorePublisherException() {
            String orderId = "TO-001";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);
            doThrow(new RuntimeException("kafka down"))
                    .when(commentEventPublisher).publishCommentEvent(any(Comment.class), eq("COMMENT_CREATED"));

            try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
                mocked.when(GeneralUtils::getCurrentUsername).thenReturn("Doctor A");

                RestResponse<CommentResponse> response =
                        commentService.createComment(orderId, createCommentRequest);

                assertThat(response.getStatusCode()).isEqualTo(200);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GET ALL COMMENTS TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get All Comments Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetAllCommentsTests {

        @Test
        @Order(1)
        @DisplayName("Should return all comments sorted by creation date")
        void shouldReturnAllCommentsSorted() {
            String orderId = "TO-001";

            Comment comment1 = new Comment();
            comment1.setCommentId("C-001");
            comment1.setCommentText("First comment");
            comment1.setCreatedBy("User 1");
            comment1.setCreatedAt(LocalDateTime.now().minusDays(1));

            Comment comment2 = new Comment();
            comment2.setCommentId("C-002");
            comment2.setCommentText("Second comment");
            comment2.setCreatedBy("User 2");
            comment2.setCreatedAt(LocalDateTime.now());

            CommentResponse response1 = CommentResponse.builder()
                    .commentId("C-001")
                    .commentText("First comment")
                    .createdBy("User 1")
                    .build();

            CommentResponse response2 = CommentResponse.builder()
                    .commentId("C-002")
                    .commentText("Second comment")
                    .createdBy("User 2")
                    .build();

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId))
                    .thenReturn(Arrays.asList(comment2, comment1));
            when(commentMapper.toCommentResponse(comment1)).thenReturn(response1);
            when(commentMapper.toCommentResponse(comment2)).thenReturn(response2);

            List<CommentResponse> response = commentService.getAllComments(orderId);

            assertThat(response).isNotNull();
            assertThat(response).hasSize(2);
            assertThat(response.get(0).getCommentId()).isEqualTo("C-001");
            assertThat(response.get(1).getCommentId()).isEqualTo("C-002");

            verify(testOrderRepository).findById(orderId);
            verify(commentRepository).findAllByTestOrder_TestOrderId(orderId);
        }

        @Test
        @Order(2)
        @DisplayName("Should throw ResourceNotFoundException when test order not found")
        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
            String invalidOrderId = "INVALID-ID";
            when(testOrderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.getAllComments(invalidOrderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");
        }

        @Test
        @Order(3)
        @DisplayName("Should throw ResourceNotFoundException when order deleted")
        void shouldThrowWhenOrderDeleted() {
            testOrder.setDeleted(true);
            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> commentService.getAllComments("TO-001"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");
        }

        @Test
        @Order(4)
        @DisplayName("Should return empty list when no comments found")
        void shouldReturnEmptyListWhenNoCommentsFound() {
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId))
                    .thenReturn(Collections.emptyList());

            List<CommentResponse> response = commentService.getAllComments(orderId);

            assertThat(response).isNotNull();
            assertThat(response).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE COMMENT TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Update Comment Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdateCommentTests {

        @Test
        @Order(1)
        @DisplayName("Should update comment successfully")
        void shouldUpdateCommentSuccessfully() {
            String orderId = "TO-001";
            String commentId = "C-001";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
                mocked.when(GeneralUtils::getCurrentUsername).thenReturn("Doctor A");

                RestResponse<CommentResponse> response =
                        commentService.updateComment(orderId, commentId, updateCommentRequest);

                assertThat(response.getStatusCode()).isEqualTo(200);
                assertThat(response.getMessage()).isEqualTo("Comment updated successfully");
            }

            verify(commentEventPublisher).publishCommentEvent(any(Comment.class), eq("COMMENT_UPDATED"));
        }

        @Test
        @Order(2)
        @DisplayName("Should throw IllegalArgumentException when request is null")
        void shouldThrowWhenRequestNull() {
            assertThatThrownBy(() -> commentService.updateComment("TO-001", "C-001", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("No comment data provided");
        }

        @Test
        @Order(3)
        @DisplayName("Should throw ResourceNotFoundException when comment not found")
        void shouldThrowResourceNotFoundExceptionWhenCommentNotFound() {
            String orderId = "TO-001";
            String invalidCommentId = "INVALID-ID";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(invalidCommentId, orderId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.updateComment(orderId, invalidCommentId, updateCommentRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Comment not found");
        }

        @Test
        @Order(4)
        @DisplayName("Should throw IllegalStateException when user not owner")
        void shouldThrowWhenNotOwner() {
            String orderId = "TO-001";
            String commentId = "C-001";
            comment.setCreatedBy("Other");

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));

            try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
                mocked.when(GeneralUtils::getCurrentUsername).thenReturn("Doctor A");

                assertThatThrownBy(() -> commentService.updateComment(orderId, commentId, updateCommentRequest))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("User does not have permission to update this comment");
            }
        }

        @Test
        @Order(5)
        @DisplayName("Should ignore publisher exception")
        void shouldIgnorePublisherException() {
            String orderId = "TO-001";
            String commentId = "C-001";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);
            doThrow(new RuntimeException("kafka down"))
                    .when(commentEventPublisher).publishCommentEvent(any(Comment.class), eq("COMMENT_UPDATED"));

            try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
                mocked.when(GeneralUtils::getCurrentUsername).thenReturn("Doctor A");

                RestResponse<CommentResponse> response =
                        commentService.updateComment(orderId, commentId, updateCommentRequest);

                assertThat(response.getStatusCode()).isEqualTo(200);
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE COMMENT TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Delete Comment Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteCommentTests {

        @Test
        @Order(1)
        @DisplayName("Should delete comment successfully")
        void shouldDeleteCommentSuccessfully() {
            String orderId = "TO-001";
            String commentId = "C-001";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            doNothing().when(commentRepository).deleteById(commentId);

            try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
                mocked.when(GeneralUtils::getCurrentUsername).thenReturn("Doctor A");

                RestResponse<Void> response = commentService.deleteComment(orderId, commentId);

                assertThat(response.getStatusCode()).isEqualTo(200);
                assertThat((String) response.getMessage())
                        .contains("Comment " + commentId + " deleted successfully");
            }

            verify(commentRepository).deleteById(commentId);
            verify(commentEventPublisher).publishCommentEvent(any(Comment.class), eq("COMMENT_DELETED"));
        }

        @Test
        @Order(2)
        @DisplayName("Should throw ResourceNotFoundException when comment not found")
        void shouldThrowResourceNotFoundExceptionWhenCommentNotFound() {
            String orderId = "TO-001";
            String invalidCommentId = "INVALID-ID";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(invalidCommentId, orderId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.deleteComment(orderId, invalidCommentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Comment not found");
        }

        @Test
        @Order(3)
        @DisplayName("Should throw IllegalStateException when user not owner")
        void shouldThrowWhenNotOwner() {
            String orderId = "TO-001";
            String commentId = "C-001";
            comment.setCreatedBy("Other");

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));

            try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
                mocked.when(GeneralUtils::getCurrentUsername).thenReturn("Doctor A");

                assertThatThrownBy(() -> commentService.deleteComment(orderId, commentId))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("User does not have permission to delete this comment");
            }
        }

        @Test
        @Order(4)
        @DisplayName("Should ignore publisher exception")
        void shouldIgnorePublisherException() {
            String orderId = "TO-001";
            String commentId = "C-001";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            doThrow(new RuntimeException("kafka down"))
                    .when(commentEventPublisher).publishCommentEvent(any(Comment.class), eq("COMMENT_DELETED"));

            try (MockedStatic<GeneralUtils> mocked = mockStatic(GeneralUtils.class)) {
                mocked.when(GeneralUtils::getCurrentUsername).thenReturn("Doctor A");

                RestResponse<Void> response = commentService.deleteComment(orderId, commentId);

                assertThat(response.getStatusCode()).isEqualTo(200);
            }
        }
    }
}

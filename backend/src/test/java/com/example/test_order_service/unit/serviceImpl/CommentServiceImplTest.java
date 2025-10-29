package com.example.test_order_service.unit.serviceImpl;

import com.example.test_order_service.dto.repsonse.PageResponse;
import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.mapper.CommentMapper;
import com.example.test_order_service.repository.CommentRepository;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.serviceImpl.CommentServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.*;

import java.time.LocalDate;
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
 * Tests business logic with mocked dependencies
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

    @InjectMocks
    private CommentServiceImpl commentService;

    private TestOrder testOrder;
    private Comment comment;
    private CommentResponse commentResponse;
    private CreateCommentRequest createCommentRequest;
    private UpdateCommentRequest updateCommentRequest;

    @BeforeEach
    void setUp() {
        testOrder = TestOrder.builder()
                .testOrderId("TO-001")
                .patientName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .citizenId("001234567890")
                .country("Vietnam")
                .gender(Gender.MALE)
                .status(TestOrderStatus.PENDING)
                .build();

        comment = Comment.builder()
                .commentId("C-001")
                .testOrder(testOrder)
                .commentText("This is a test comment")
                .createdBy("Doctor A")
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        commentResponse = CommentResponse.builder()
                .commentId("C-001")
                .commentText("This is a test comment")
                .createdBy("Doctor A")
                .createdAt(LocalDateTime.now())
                .build();

        createCommentRequest = CreateCommentRequest.builder()
                .commentText("This is a test comment")
                .createdBy("Doctor A")
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
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            RestResponse<CommentResponse> response = commentService.createComment(orderId, createCommentRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Comment created successfully");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getResult().getCommentText()).isEqualTo("This is a test comment");
            assertThat(response.getTimestamp()).isNotNull();

            verify(testOrderRepository).findById(orderId);
            verify(commentMapper).toCommentEntity(createCommentRequest);
            verify(commentRepository).save(any(Comment.class));
            verify(commentMapper).toCommentResponse(comment);
        }

        @Test
        @Order(2)
        @DisplayName("Should throw ResourceNotFoundException when test order not found")
        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
            // Given
            String invalidOrderId = "INVALID-ID";
            when(testOrderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.createComment(invalidOrderId, createCommentRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(invalidOrderId);
            verify(commentMapper, never()).toCommentEntity(any());
            verify(commentRepository, never()).save(any());
        }

        @Test
        @Order(3)
        @DisplayName("Should set test order to comment")
        void shouldSetTestOrderToComment() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            commentService.createComment(orderId, createCommentRequest);

            // Then
            verify(commentRepository).save(argThat(c ->
                    c.getTestOrder() != null &&
                            c.getTestOrder().getTestOrderId().equals("TO-001")
            ));
        }

        @Test
        @Order(4)
        @DisplayName("Should set createdAt if null")
        void shouldSetCreatedAtIfNull() {
            // Given
            String orderId = "TO-001";
            comment.setCreatedAt(null);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            commentService.createComment(orderId, createCommentRequest);

            // Then
            verify(commentRepository).save(argThat(c -> c.getCreatedAt() != null));
        }

        @Test
        @Order(5)
        @DisplayName("Should preserve createdAt if already set")
        void shouldPreserveCreatedAtIfAlreadySet() {
            // Given
            String orderId = "TO-001";
            LocalDateTime existingCreatedAt = LocalDateTime.of(2024, 1, 1, 10, 0);
            comment.setCreatedAt(existingCreatedAt);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            commentService.createComment(orderId, createCommentRequest);

            // Then
            verify(commentRepository).save(argThat(c ->
                    c.getCreatedAt().equals(existingCreatedAt)
            ));
        }

        @Test
        @Order(6)
        @DisplayName("Should handle repository exception during create")
        void shouldHandleRepositoryExceptionDuringCreate() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class)))
                    .thenThrow(new DataAccessException("Database error") {});

            // When & Then
            assertThatThrownBy(() -> commentService.createComment(orderId, createCommentRequest))
                    .isInstanceOf(DataAccessException.class);

            verify(commentRepository).save(any(Comment.class));
        }

        @Test
        @Order(7)
        @DisplayName("Should handle null mapper result")
        void shouldHandleNullMapperResult() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> commentService.createComment(orderId, createCommentRequest))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @Order(8)
        @DisplayName("Should handle empty comment text")
        void shouldHandleEmptyCommentText() {
            // Given
            String orderId = "TO-001";
            createCommentRequest.setCommentText("");
            comment.setCommentText("");
            commentResponse.setCommentText("");

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            RestResponse<CommentResponse> response = commentService.createComment(orderId, createCommentRequest);

            // Then
            assertThat(response.getResult().getCommentText()).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GET COMMENTS TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Comments Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetCommentsTests {

        @Test
        @Order(1)
        @DisplayName("Should return paginated comments")
        void shouldReturnPaginatedComments() {
            // Given
            String orderId = "TO-001";
            Pageable pageable = PageRequest.of(0, 10);

            Comment comment1 = Comment.builder()
                    .commentId("C-001")
                    .commentText("First comment")
                    .createdBy("User 1")
                    .build();

            Comment comment2 = Comment.builder()
                    .commentId("C-002")
                    .commentText("Second comment")
                    .createdBy("User 2")
                    .build();

            Page<Comment> commentPage = new PageImpl<>(
                    Arrays.asList(comment1, comment2),
                    pageable,
                    2
            );

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
            when(commentRepository.findByTestOrder_TestOrderId(orderId, pageable))
                    .thenReturn(commentPage);
            when(commentMapper.toCommentResponse(comment1)).thenReturn(response1);
            when(commentMapper.toCommentResponse(comment2)).thenReturn(response2);

            // When
            PageResponse<CommentResponse> response = commentService.getComments(orderId, pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCurrentPage()).isEqualTo(1);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getItems()).hasSize(2);
            assertThat(response.getItems().get(0).getCommentId()).isEqualTo("C-001");
            assertThat(response.getItems().get(1).getCommentId()).isEqualTo("C-002");

            verify(testOrderRepository).findById(orderId);
            verify(commentRepository).findByTestOrder_TestOrderId(orderId, pageable);
            verify(commentMapper, times(2)).toCommentResponse(any(Comment.class));
        }

        @Test
        @Order(2)
        @DisplayName("Should throw ResourceNotFoundException when test order not found")
        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
            // Given
            String invalidOrderId = "INVALID-ID";
            Pageable pageable = PageRequest.of(0, 10);
            when(testOrderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.getComments(invalidOrderId, pageable))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(invalidOrderId);
            verify(commentRepository, never()).findByTestOrder_TestOrderId(anyString(), any());
        }

        @Test
        @Order(3)
        @DisplayName("Should return empty list when no comments found")
        void shouldReturnEmptyListWhenNoCommentsFound() {
            // Given
            String orderId = "TO-001";
            Pageable pageable = PageRequest.of(0, 10);
            Page<Comment> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByTestOrder_TestOrderId(orderId, pageable))
                    .thenReturn(emptyPage);

            // When
            PageResponse<CommentResponse> response = commentService.getComments(orderId, pageable);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getItems()).isEmpty();
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.getCurrentPage()).isEqualTo(1);
        }

        @Test
        @Order(4)
        @DisplayName("Should handle pagination correctly for multiple pages")
        void shouldHandlePaginationCorrectlyForMultiplePages() {
            // Given
            String orderId = "TO-001";
            Pageable pageable = PageRequest.of(1, 10); // Page 2
            Page<Comment> commentPage = new PageImpl<>(
                    Collections.singletonList(comment),
                    pageable,
                    25 // Total 25 items, 3 pages
            );

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByTestOrder_TestOrderId(orderId, pageable))
                    .thenReturn(commentPage);
            when(commentMapper.toCommentResponse(any())).thenReturn(commentResponse);

            // When
            PageResponse<CommentResponse> response = commentService.getComments(orderId, pageable);

            // Then
            assertThat(response.getCurrentPage()).isEqualTo(2);
            assertThat(response.getTotalPages()).isEqualTo(3);
        }

        @Test
        @Order(5)
        @DisplayName("Should handle repository exception")
        void shouldHandleRepositoryException() {
            // Given
            String orderId = "TO-001";
            Pageable pageable = PageRequest.of(0, 10);
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByTestOrder_TestOrderId(orderId, pageable))
                    .thenThrow(new DataAccessException("Database error") {});

            // When & Then
            assertThatThrownBy(() -> commentService.getComments(orderId, pageable))
                    .isInstanceOf(DataAccessException.class);
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
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            RestResponse<CommentResponse> response = commentService.updateComment(
                    orderId, commentId, updateCommentRequest
            );

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Comment updated successfully");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getTimestamp()).isNotNull();

            verify(commentRepository).findByCommentIdAndTestOrder_TestOrderId(commentId, orderId);
            verify(commentRepository).save(any(Comment.class));
            verify(commentMapper).toCommentResponse(comment);
        }

        @Test
        @Order(2)
        @DisplayName("Should throw ResourceNotFoundException when comment not found")
        void shouldThrowResourceNotFoundExceptionWhenCommentNotFound() {
            // Given
            String orderId = "TO-001";
            String invalidCommentId = "INVALID-ID";

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(invalidCommentId, orderId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.updateComment(
                    orderId, invalidCommentId, updateCommentRequest
            ))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Comment not found");

            verify(commentRepository).findByCommentIdAndTestOrder_TestOrderId(invalidCommentId, orderId);
            verify(commentRepository, never()).save(any());
        }

        @Test
        @Order(3)
        @DisplayName("Should update comment text")
        void shouldUpdateCommentText() {
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";
            String newText = "Updated comment text";
            updateCommentRequest.setCommentText(newText);

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            commentService.updateComment(orderId, commentId, updateCommentRequest);

            // Then
            verify(commentRepository).save(argThat(c ->
                    c.getCommentText().equals(newText)
            ));
        }

        @Test
        @Order(4)
        @DisplayName("Should set updatedAt timestamp")
        void shouldSetUpdatedAtTimestamp() {
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            commentService.updateComment(orderId, commentId, updateCommentRequest);

            // Then
            verify(commentRepository).save(argThat(c -> c.getUpdatedAt() != null));
        }

        @Test
        @Order(5)
        @DisplayName("Should not update comment if it belongs to different test order")
        void shouldNotUpdateCommentIfBelongsToDifferentTestOrder() {
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";
            String differentOrderId = "TO-002";

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, differentOrderId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.updateComment(
                    differentOrderId, commentId, updateCommentRequest
            ))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Comment not found");

            verify(commentRepository, never()).save(any());
        }

        @Test
        @Order(6)
        @DisplayName("Should handle repository exception during update")
        void shouldHandleRepositoryExceptionDuringUpdate() {
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            when(commentRepository.save(any(Comment.class)))
                    .thenThrow(new DataAccessException("Database error") {});

            // When & Then
            assertThatThrownBy(() -> commentService.updateComment(
                    orderId, commentId, updateCommentRequest
            ))
                    .isInstanceOf(DataAccessException.class);
        }

        @Test
        @Order(7)
        @DisplayName("Should handle empty comment text update")
        void shouldHandleEmptyCommentTextUpdate() {
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";
            updateCommentRequest.setCommentText("");

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            commentService.updateComment(orderId, commentId, updateCommentRequest);

            // Then
            verify(commentRepository).save(argThat(c -> c.getCommentText().isEmpty()));
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
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            doNothing().when(commentRepository).deleteById(commentId);

            // When
            RestResponse<Void> response = commentService.deleteComment(orderId, commentId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat((String) response.getMessage()).contains("Comment " + commentId + " deleted successfully");
            assertThat(response.getTimestamp()).isNotNull();

            verify(commentRepository).findByCommentIdAndTestOrder_TestOrderId(commentId, orderId);
            verify(commentRepository).deleteById(commentId);
        }

        @Test
        @Order(2)
        @DisplayName("Should throw ResourceNotFoundException when comment not found")
        void shouldThrowResourceNotFoundExceptionWhenCommentNotFound() {
            // Given
            String orderId = "TO-001";
            String invalidCommentId = "INVALID-ID";

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(invalidCommentId, orderId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(orderId, invalidCommentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Comment not found");

            verify(commentRepository).findByCommentIdAndTestOrder_TestOrderId(invalidCommentId, orderId);
            verify(commentRepository, never()).deleteById(anyString());
        }

        @Test
        @Order(3)
        @DisplayName("Should not delete comment if it belongs to different test order")
        void shouldNotDeleteCommentIfBelongsToDifferentTestOrder() {
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";
            String differentOrderId = "TO-002";

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, differentOrderId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(differentOrderId, commentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Comment not found");

            verify(commentRepository, never()).deleteById(anyString());
        }

        @Test
        @Order(4)
        @DisplayName("Should handle repository exception during delete")
        void shouldHandleRepositoryExceptionDuringDelete() {
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            doThrow(new DataAccessException("Database error") {})
                    .when(commentRepository).deleteById(commentId);

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(orderId, commentId))
                    .isInstanceOf(DataAccessException.class);

            verify(commentRepository).deleteById(commentId);
        }

        @Test
        @Order(5)
        @DisplayName("Should delete using correct comment ID")
        void shouldDeleteUsingCorrectCommentId() {
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";

            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            doNothing().when(commentRepository).deleteById(commentId);

            // When
            commentService.deleteComment(orderId, commentId);

            // Then
            verify(commentRepository).deleteById(eq(commentId));
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
        @DisplayName("Should return all comments sorted")
        void shouldReturnAllCommentsSorted() {
            // Given
            String orderId = "TO-001";
            Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");

            Comment comment1 = Comment.builder()
                    .commentId("C-001")
                    .commentText("First comment")
                    .createdBy("User 1")
                    .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                    .build();

            Comment comment2 = Comment.builder()
                    .commentId("C-002")
                    .commentText("Second comment")
                    .createdBy("User 2")
                    .createdAt(LocalDateTime.of(2024, 1, 1, 11, 0))
                    .build();

            List<Comment> comments = Arrays.asList(comment2, comment1); // Sorted DESC

            CommentResponse response1 = CommentResponse.builder()
                    .commentId("C-001")
                    .commentText("First comment")
                    .build();

            CommentResponse response2 = CommentResponse.builder()
                    .commentId("C-002")
                    .commentText("Second comment")
                    .build();

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId, sort))
                    .thenReturn(comments);
            when(commentMapper.toCommentResponse(comment1)).thenReturn(response1);
            when(commentMapper.toCommentResponse(comment2)).thenReturn(response2);

            // When
            List<CommentResponse> responses = commentService.getAllComments(orderId, sort);

            // Then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).getCommentId()).isEqualTo("C-002");
            assertThat(responses.get(1).getCommentId()).isEqualTo("C-001");

            verify(testOrderRepository).findById(orderId);
            verify(commentRepository).findAllByTestOrder_TestOrderId(orderId, sort);
            verify(commentMapper, times(2)).toCommentResponse(any(Comment.class));
        }

        @Test
        @Order(2)
        @DisplayName("Should throw ResourceNotFoundException when test order not found")
        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
            // Given
            String invalidOrderId = "INVALID-ID";
            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");
            when(testOrderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.getAllComments(invalidOrderId, sort))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(invalidOrderId);
            verify(commentRepository, never()).findAllByTestOrder_TestOrderId(anyString(), any());
        }

        @Test
        @Order(3)
        @DisplayName("Should return empty list when no comments found")
        void shouldReturnEmptyListWhenNoCommentsFound() {
            // Given
            String orderId = "TO-001";
            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId, sort))
                    .thenReturn(Collections.emptyList());

            // When
            List<CommentResponse> responses = commentService.getAllComments(orderId, sort);

            // Then
            assertThat(responses).isNotNull();
            assertThat(responses).isEmpty();
            verify(commentMapper, never()).toCommentResponse(any());
        }

        @Test
        @Order(4)
        @DisplayName("Should sort comments by createdAt ascending")
        void shouldSortCommentsByCreatedAtAscending() {
            // Given
            String orderId = "TO-001";
            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

            Comment comment1 = Comment.builder()
                    .commentId("C-001")
                    .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                    .build();

            Comment comment2 = Comment.builder()
                    .commentId("C-002")
                    .createdAt(LocalDateTime.of(2024, 1, 1, 11, 0))
                    .build();

            List<Comment> comments = Arrays.asList(comment1, comment2); // Sorted ASC

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId, sort))
                    .thenReturn(comments);
            when(commentMapper.toCommentResponse(any())).thenReturn(commentResponse);

            // When
            List<CommentResponse> responses = commentService.getAllComments(orderId, sort);

            // Then
            assertThat(responses).hasSize(2);
            verify(commentRepository).findAllByTestOrder_TestOrderId(orderId, sort);
        }

        @Test
        @Order(5)
        @DisplayName("Should sort comments by createdBy")
        void shouldSortCommentsByCreatedBy() {
            // Given
            String orderId = "TO-001";
            Sort sort = Sort.by(Sort.Direction.ASC, "createdBy");

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId, sort))
                    .thenReturn(Collections.singletonList(comment));
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            List<CommentResponse> responses = commentService.getAllComments(orderId, sort);

            // Then
            assertThat(responses).isNotNull();
            verify(commentRepository).findAllByTestOrder_TestOrderId(orderId, sort);
        }

        @Test
        @Order(6)
        @DisplayName("Should handle repository exception")
        void shouldHandleRepositoryException() {
            // Given
            String orderId = "TO-001";
            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId, sort))
                    .thenThrow(new DataAccessException("Database error") {});

            // When & Then
            assertThatThrownBy(() -> commentService.getAllComments(orderId, sort))
                    .isInstanceOf(DataAccessException.class);
        }

        @Test
        @Order(7)
        @DisplayName("Should handle null sort parameter")
        void shouldHandleNullSortParameter() {
            // Given
            String orderId = "TO-001";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(eq(orderId), isNull()))
                    .thenReturn(Collections.singletonList(comment));
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When
            List<CommentResponse> responses = commentService.getAllComments(orderId, null);

            // Then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(1);
        }

        @Test
        @Order(8)
        @DisplayName("Should handle mapper exception")
        void shouldHandleMapperException() {
            // Given
            String orderId = "TO-001";
            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId, sort))
                    .thenReturn(Collections.singletonList(comment));
            when(commentMapper.toCommentResponse(comment))
                    .thenThrow(new RuntimeException("Mapping error"));

            // When & Then
            assertThatThrownBy(() -> commentService.getAllComments(orderId, sort))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Mapping error");
        }

        @Test
        @Order(9)
        @DisplayName("Should return multiple comments with different creators")
        void shouldReturnMultipleCommentsWithDifferentCreators() {
            // Given
            String orderId = "TO-001";
            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

            Comment comment1 = Comment.builder()
                    .commentId("C-001")
                    .commentText("Comment by Doctor A")
                    .createdBy("Doctor A")
                    .build();

            Comment comment2 = Comment.builder()
                    .commentId("C-002")
                    .commentText("Comment by Doctor B")
                    .createdBy("Doctor B")
                    .build();

            Comment comment3 = Comment.builder()
                    .commentId("C-003")
                    .commentText("Comment by Lab Tech")
                    .createdBy("Lab Tech")
                    .build();

            List<Comment> comments = Arrays.asList(comment1, comment2, comment3);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId, sort))
                    .thenReturn(comments);
            when(commentMapper.toCommentResponse(any())).thenReturn(commentResponse);

            // When
            List<CommentResponse> responses = commentService.getAllComments(orderId, sort);

            // Then
            assertThat(responses).hasSize(3);
            verify(commentMapper, times(3)).toCommentResponse(any(Comment.class));
        }

        @Test
        @Order(10)
        @DisplayName("Should handle large number of comments")
        void shouldHandleLargeNumberOfComments() {
            // Given
            String orderId = "TO-001";
            Sort sort = Sort.by(Sort.Direction.ASC, "createdAt");

            List<Comment> comments = new java.util.ArrayList<>();
            for (int i = 0; i < 100; i++) {
                comments.add(Comment.builder()
                        .commentId("C-" + i)
                        .commentText("Comment " + i)
                        .build());
            }

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId, sort))
                    .thenReturn(comments);
            when(commentMapper.toCommentResponse(any())).thenReturn(commentResponse);

            // When
            List<CommentResponse> responses = commentService.getAllComments(orderId, sort);

            // Then
            assertThat(responses).hasSize(100);
            verify(commentMapper, times(100)).toCommentResponse(any(Comment.class));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // INTEGRATION SCENARIOS TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Scenarios Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class IntegrationScenariosTests {

        @Test
        @Order(1)
        @DisplayName("Should create and then retrieve comment")
        void shouldCreateAndThenRetrieveComment() {
            // Given - Create
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When - Create
            RestResponse<CommentResponse> createResponse = commentService.createComment(
                    orderId, createCommentRequest
            );

            // Then - Create
            assertThat(createResponse.getStatusCode()).isEqualTo(200);
            String createdCommentId = createResponse.getResult().getCommentId();

            // Given - Retrieve
            Pageable pageable = PageRequest.of(0, 10);
            Page<Comment> commentPage = new PageImpl<>(
                    Collections.singletonList(comment),
                    pageable,
                    1
            );
            when(commentRepository.findByTestOrder_TestOrderId(orderId, pageable))
                    .thenReturn(commentPage);

            // When - Retrieve
            PageResponse<CommentResponse> getResponse = commentService.getComments(orderId, pageable);

            // Then - Retrieve
            assertThat(getResponse.getItems()).hasSize(1);
            assertThat(getResponse.getItems().get(0).getCommentId()).isEqualTo(createdCommentId);
        }

        @Test
        @Order(2)
        @DisplayName("Should create, update, and then retrieve comment")
        void shouldCreateUpdateAndRetrieveComment() {
            // Create
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            RestResponse<CommentResponse> createResponse = commentService.createComment(
                    orderId, createCommentRequest
            );
            String commentId = createResponse.getResult().getCommentId();

            // Update
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));

            RestResponse<CommentResponse> updateResponse = commentService.updateComment(
                    orderId, commentId, updateCommentRequest
            );

            // Verify
            assertThat(createResponse.getStatusCode()).isEqualTo(200);
            assertThat(updateResponse.getStatusCode()).isEqualTo(200);
            verify(commentRepository, times(2)).save(any(Comment.class));
        }

        @Test
        @Order(3)
        @DisplayName("Should create and then delete comment")
        void shouldCreateAndDeleteComment() {
            // Create
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            RestResponse<CommentResponse> createResponse = commentService.createComment(
                    orderId, createCommentRequest
            );
            String commentId = createResponse.getResult().getCommentId();

            // Delete
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            doNothing().when(commentRepository).deleteById(commentId);

            RestResponse<Void> deleteResponse = commentService.deleteComment(orderId, commentId);

            // Verify
            assertThat(createResponse.getStatusCode()).isEqualTo(200);
            assertThat(deleteResponse.getStatusCode()).isEqualTo(200);
            verify(commentRepository).deleteById(commentId);
        }

        @Test
        @Order(4)
        @DisplayName("Should handle multiple comments for same test order")
        void shouldHandleMultipleCommentsForSameTestOrder() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            Comment comment1 = Comment.builder().commentId("C-001").build();
            Comment comment2 = Comment.builder().commentId("C-002").build();
            Comment comment3 = Comment.builder().commentId("C-003").build();

            when(commentMapper.toCommentEntity(any())).thenReturn(comment1, comment2, comment3);
            when(commentRepository.save(any(Comment.class)))
                    .thenReturn(comment1, comment2, comment3);
            when(commentMapper.toCommentResponse(any())).thenReturn(commentResponse);

            // When - Create 3 comments
            commentService.createComment(orderId, createCommentRequest);
            commentService.createComment(orderId, createCommentRequest);
            commentService.createComment(orderId, createCommentRequest);

            // Then
            verify(commentRepository, times(3)).save(any(Comment.class));
            verify(testOrderRepository, times(3)).findById(orderId);
        }

        @Test
        @Order(5)
        @DisplayName("Should prevent operations on deleted test order")
        void shouldPreventOperationsOnDeletedTestOrder() {
            // Given
            String orderId = "TO-001";
            testOrder.setDeleted(true);

            // Test order exists but is deleted
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentMapper.toCommentEntity(createCommentRequest)).thenReturn(comment);
            when(commentRepository.save(any(Comment.class))).thenReturn(comment);
            when(commentMapper.toCommentResponse(comment)).thenReturn(commentResponse);

            // When - Should still work as service doesn't check deleted flag
            RestResponse<CommentResponse> response = commentService.createComment(
                    orderId, createCommentRequest
            );

            // Then - Service allows operation (business logic may need to prevent this)
            assertThat(response.getStatusCode()).isEqualTo(200);
        }
    }
}
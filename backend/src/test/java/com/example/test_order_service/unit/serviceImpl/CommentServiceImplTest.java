package com.example.test_order_service.unit.serviceImpl;

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
    }

    // ═══════════════════════════════════════════════════════════════
    // GET COMMENTS TESTS
    // ═══════════════════════════════════════════════════════════════



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
            // Given
            String orderId = "TO-001";

            Comment comment1 = Comment.builder()
                    .commentId("C-001")
                    .commentText("First comment")
                    .createdBy("User 1")
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build();

            Comment comment2 = Comment.builder()
                    .commentId("C-002")
                    .commentText("Second comment")
                    .createdBy("User 2")
                    .createdAt(LocalDateTime.now())
                    .build();

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
                    .thenReturn(Arrays.asList(comment2, comment1)); // Unordered list
            when(commentMapper.toCommentResponse(comment1)).thenReturn(response1);
            when(commentMapper.toCommentResponse(comment2)).thenReturn(response2);

            // When
            List<CommentResponse> response = commentService.getAllComments(orderId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response).hasSize(2);
            assertThat(response.get(0).getCommentId()).isEqualTo("C-001"); // Sorted by createdAt
            assertThat(response.get(1).getCommentId()).isEqualTo("C-002");

            verify(testOrderRepository).findById(orderId);
            verify(commentRepository).findAllByTestOrder_TestOrderId(orderId);
            verify(commentMapper, times(2)).toCommentResponse(any(Comment.class));
        }

        @Test
        @Order(2)
        @DisplayName("Should throw ResourceNotFoundException when test order not found")
        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
            // Given
            String invalidOrderId = "INVALID-ID";
            when(testOrderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> commentService.getAllComments(invalidOrderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(invalidOrderId);
            verify(commentRepository, never()).findAllByTestOrder_TestOrderId(anyString());
        }

        @Test
        @Order(3)
        @DisplayName("Should return empty list when no comments found")
        void shouldReturnEmptyListWhenNoCommentsFound() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId))
                    .thenReturn(Collections.emptyList());

            // When
            List<CommentResponse> response = commentService.getAllComments(orderId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response).isEmpty();
        }

        @Test
        @Order(4)
        @DisplayName("Should handle repository exception")
        void shouldHandleRepositoryException() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findAllByTestOrder_TestOrderId(orderId))
                    .thenThrow(new DataAccessException("Database error") {});

            // When & Then
            assertThatThrownBy(() -> commentService.getAllComments(orderId))
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

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
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

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
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

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
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
        @DisplayName("Should handle repository exception during update")
        void shouldHandleRepositoryExceptionDuringUpdate() {
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
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

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
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

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
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
        @DisplayName("Should handle repository exception during delete")
        void shouldHandleRepositoryExceptionDuringDelete() {
            // Given
            String orderId = "TO-001";
            String commentId = "C-001";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId))
                    .thenReturn(Optional.of(comment));
            doThrow(new DataAccessException("Database error") {})
                    .when(commentRepository).deleteById(commentId);

            // When & Then
            assertThatThrownBy(() -> commentService.deleteComment(orderId, commentId))
                    .isInstanceOf(DataAccessException.class);

            verify(commentRepository).deleteById(commentId);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GET ALL COMMENTS TESTS
    // ═══════════════════════════════════════════════════════════════



    // ═══════════════════════════════════════════════════════════════
    // INTEGRATION SCENARIOS TESTS
    // ═══════════════════════════════════════════════════════════════


}
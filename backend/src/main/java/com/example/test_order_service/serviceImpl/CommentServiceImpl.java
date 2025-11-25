package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.request.AIReviewRequest;
import com.example.test_order_service.dto.response.AIReviewResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.exception.ResourceNotFoundException;
// THÊM IMPORT
import com.example.test_order_service.ingest.publisher.CommentEventPublisher;
import com.example.test_order_service.mapper.CommentMapper;
import com.example.test_order_service.repository.CommentRepository;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.service.CommentService;
import com.example.test_order_service.utils.GeneralUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TestOrderRepository testOrderRepository;
    private final CommentMapper commentMapper;
    private final CommentEventPublisher commentEventPublisher;
    private final WebClient webClient;

    @Override
    public RestResponse<CommentResponse> createComment(String orderId, CreateCommentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("No comment data provided"); // Sửa message
        }

        TestOrder testOrder = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        if (testOrder.isDeleted()) {
            throw new ResourceNotFoundException("Test order not found");
        }

        Comment comment = commentMapper.toCommentEntity(request);
        comment.setTestOrder(testOrder);
        comment.setCreatedBy(GeneralUtils.getCurrentUsername());

        // Lưu vào DB
        Comment saved = commentRepository.save(comment);

        // Gửi sự kiện Kafka sau khi lưu thành công
        try {
            commentEventPublisher.publishCommentEvent(saved, "COMMENT_CREATED");
        } catch (Exception e) {
            // Log lỗi nhưng không làm fail request
            System.err.println("Failed to publish COMMENT_CREATED event: " + e.getMessage());
        }

        CommentResponse response = commentMapper.toCommentResponse(saved);

        return RestResponse.<CommentResponse>builder()
                .statusCode(200)
                .message("Comment created successfully")
                .result(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public RestResponse<CommentResponse> updateComment(String orderId, String commentId, UpdateCommentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("No comment data provided"); // Sửa message
        }

        // Không cần query testOrder nếu không dùng
        TestOrder testOrder = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));
        if (testOrder.isDeleted()) {
            throw new ResourceNotFoundException("Test order not found");
        }

        Comment comment = commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        String currentUsername = GeneralUtils.getCurrentUsername();
        if (!comment.getCreatedBy().equals(currentUsername)) {
            throw new IllegalStateException("User does not have permission to update this comment");
        }

        // Luôn cập nhật text mới từ request
        comment.setCommentText(request.getCommentText());
        comment.setUpdatedBy(GeneralUtils.getCurrentUsername());

        // Lưu vào DB
        Comment saved = commentRepository.save(comment);

        // Gửi sự kiện Kafka sau khi lưu thành công
        try {
            commentEventPublisher.publishCommentEvent(saved, "COMMENT_UPDATED");
        } catch (Exception e) {
            System.err.println("Failed to publish COMMENT_UPDATED event: " + e.getMessage());
        }

        CommentResponse response = commentMapper.toCommentResponse(saved);

        return RestResponse.<CommentResponse>builder()
                .statusCode(200)
                .message("Comment updated successfully")
                .result(response)
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public RestResponse<Void> deleteComment(String orderId, String commentId) {
        // Không cần query testOrder nếu bạn không dùng
        TestOrder order = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        if (order.isDeleted()) {
            throw new ResourceNotFoundException("Test order not found");
        }

        Comment comment = commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        String currentUsername = GeneralUtils.getCurrentUsername();
        if (!comment.getCreatedBy().equals(currentUsername)) {
            throw new IllegalStateException("User does not have permission to delete this comment");
        }

        // Xóa khỏi DB
        commentRepository.deleteById(comment.getCommentId());

        // --- THÊM DÒNG NÀY ---
        // Gửi sự kiện Kafka sau khi xóa thành công
        // `comment` object vẫn còn thông tin để gửi đi
        try {
            commentEventPublisher.publishCommentEvent(comment, "COMMENT_DELETED");
        } catch (Exception e) {
            System.err.println("Failed to publish COMMENT_DELETED event: " + e.getMessage());
        }

        return RestResponse.<Void>builder()
                .statusCode(200)
                .message("Comment " + commentId + " deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public List<CommentResponse> getAllComments(String orderId) {
        // Đảm bảo test order tồn tại
        TestOrder order = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        if (order.isDeleted()) {
            throw new ResourceNotFoundException("Test order not found");
        }

        return commentRepository.findAllByTestOrder_TestOrderId(orderId).stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(commentMapper::toCommentResponse)
                .toList();
    }

    @Override
    @Async("taskExecutor")
    public CompletableFuture<RestResponse<CommentResponse>> getAIReview(String orderId) {
        try {
            // Kiểm tra test order có tồn tại không
            TestOrder testOrder = testOrderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

            if (testOrder.isDeleted()) {
                throw new ResourceNotFoundException("Test order not found");
            }

            // Kiểm tra test result có tồn tại không
            TestResult testResult = testOrder.getTestResults();
            if (testResult == null) {
                throw new ResourceNotFoundException("Test result not found for this order");
            }

            // Lấy HL7 raw data
            String hl7Message = testResult.getHl7RawData();
            if (hl7Message == null || hl7Message.isEmpty()) {
                throw new ResourceNotFoundException("HL7 data not found in test result");
            }

            // Tạo request cho AI API
            AIReviewRequest aiRequest = AIReviewRequest.builder()
                    .hl7Message(hl7Message)
                    .build();

            // Gọi AI API bất đồng bộ
            return webClient.post()
                    .uri("https://medical-ai-api-xva0.onrender.com/analyze_hl7")
                    .bodyValue(aiRequest)
                    .retrieve()
                    .bodyToMono(AIReviewResponse.class)
                    .map(aiResponse -> {
                        // Tạo Comment mới với kết quả từ AI
                        Comment aiComment = Comment.builder()
                                .commentText(aiResponse.getAnalysisResult())
                                .createdBy("AI-REVIEWED")
                                .testOrder(testOrder)
                                .updatedBy("AI-REVIEWED")
                                .build();

                        // Lưu comment vào database
                        Comment savedComment = commentRepository.save(aiComment);

                        // Gửi sự kiện Kafka sau khi lưu thành công
                        try {
                            commentEventPublisher.publishCommentEvent(savedComment, "COMMENT_CREATED");
                        } catch (Exception e) {
                            System.err.println("Failed to publish COMMENT_CREATED event: " + e.getMessage());
                        }

                        // Chuyển đổi sang CommentResponse
                        CommentResponse commentResponse = commentMapper.toCommentResponse(savedComment);

                        return RestResponse.<CommentResponse>builder()
                                .statusCode(200)
                                .message("AI review completed successfully")
                                .result(commentResponse)
                                .timestamp(LocalDateTime.now())
                                .build();
                    })
                    .onErrorResume(error -> {
                        // Xử lý lỗi khi gọi AI API
                        System.err.println("Error calling AI API: " + error.getMessage());
                        return Mono.just(RestResponse.<CommentResponse>builder()
                                .statusCode(500)
                                .message("Failed to get AI review: " + error.getMessage())
                                .error(error.getMessage())
                                .timestamp(LocalDateTime.now())
                                .build());
                    })
                    .toFuture();
        } catch (Exception e) {
            // Xử lý exception xảy ra trước khi gọi AI API
            System.err.println("Error in getAIReview: " + e.getMessage());
            return CompletableFuture.completedFuture(
                    RestResponse.<CommentResponse>builder()
                            .statusCode(400)
                            .message("Bad Request: " + e.getMessage())
                            .error(e.getMessage())
                            .timestamp(LocalDateTime.now())
                            .build()
            );
        }
    }
}

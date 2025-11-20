package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.exception.ResourceNotFoundException;
// THÊM IMPORT
import com.example.test_order_service.ingest.publisher.CommentEventPublisher;
import com.example.test_order_service.mapper.CommentMapper;
import com.example.test_order_service.repository.CommentRepository;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.service.CommentService;
import com.example.test_order_service.utils.GeneralUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TestOrderRepository testOrderRepository;
    private final CommentMapper commentMapper;
    private final CommentEventPublisher commentEventPublisher;

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
}
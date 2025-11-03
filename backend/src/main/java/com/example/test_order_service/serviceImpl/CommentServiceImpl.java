package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.mapper.CommentMapper;
import com.example.test_order_service.repository.CommentRepository;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.service.CommentService;
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

    @Override
    public RestResponse<CommentResponse> createComment(String orderId, CreateCommentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("No test results provided");
        }

        TestOrder testOrder = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        Comment comment = commentMapper.toCommentEntity(request);
        comment.setTestOrder(testOrder);
        comment.setCreatedBy("System");

        Comment saved = commentRepository.save(comment);
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
            throw new IllegalArgumentException("No test results provided");
        }

        TestOrder testOrder = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        Comment comment = commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        comment.setCommentText(comment.getCommentText() != null ? request.getCommentText() : comment.getCommentText());

        Comment saved = commentRepository.save(comment);
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
        TestOrder testOrder = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        Comment comment = commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        commentRepository.deleteById(comment.getCommentId());

        return RestResponse.<Void>builder()
                .statusCode(200)
                .message("Comment " + commentId + " deleted successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public List<CommentResponse> getAllComments(String orderId) {
        // Đảm bảo test order tồn tại
        testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        return commentRepository.findAllByTestOrder_TestOrderId(orderId).stream()
                .sorted(Comparator.comparing(Comment::getCreatedAt))
                .map(commentMapper::toCommentResponse)
                .toList();
    }
}
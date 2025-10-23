package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.repsonse.PageResponse;
import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.CommentResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final TestOrderRepository testOrderRepository;
    private final CommentMapper commentMapper;

    @Override
    public RestResponse<CommentResponse> createComment(String orderId, CreateCommentRequest request) {
        TestOrder testOrder = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        Comment comment = commentMapper.toCommentEntity(request);
        comment.setTestOrder(testOrder);

        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }

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
    public PageResponse<CommentResponse> getComments(String orderId, Pageable pageable) {
        // Ensure test order exists
        testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Test order not found"));

        Page<Comment> page = commentRepository.findByTestOrder_TestOrderId(orderId, pageable);

        return PageResponse.<CommentResponse>builder()
                .currentPage(page.getNumber() + 1)
                .totalPages(page.getTotalPages())
                .items(page.stream().map(commentMapper::toCommentResponse).toList())
                .build();
    }

    @Override
    public RestResponse<CommentResponse> updateComment(String orderId, String commentId, UpdateCommentRequest request) {
        Comment comment = commentRepository.findByCommentIdAndTestOrder_TestOrderId(commentId, orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        comment.setCommentText(request.getCommentText());
        comment.setUpdatedAt(LocalDateTime.now());

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

        List<Comment> comments = commentRepository.findAllByTestOrder_TestOrderId(orderId);

        return comments.stream()
                .map(commentMapper::toCommentResponse)
                .collect(Collectors.toList());
    }
}
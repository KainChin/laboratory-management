package com.example.test_order_service.service;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public interface CommentService {
    RestResponse<CommentResponse> createComment(String orderId, CreateCommentRequest request);
    RestResponse<CommentResponse> updateComment(String orderId, String commentId, UpdateCommentRequest request);
    RestResponse<Void> deleteComment(String orderId, String commentId);
    List<CommentResponse> getAllComments(String orderId);
    CompletableFuture<RestResponse<CommentResponse>> getAIReview(String orderId);
}
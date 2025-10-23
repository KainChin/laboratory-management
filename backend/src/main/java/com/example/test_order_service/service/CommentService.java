package com.example.test_order_service.service;

import com.example.test_order_service.dto.repsonse.PageResponse;
import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CommentService {
    RestResponse<CommentResponse> createComment(String orderId, CreateCommentRequest request);
    PageResponse<CommentResponse> getComments(String orderId, Pageable pageable);
    RestResponse<CommentResponse> updateComment(String orderId, String commentId, UpdateCommentRequest request);
    RestResponse<Void> deleteComment(String orderId, String commentId);
    List<CommentResponse> getAllComments(String orderId, Sort sort);
}
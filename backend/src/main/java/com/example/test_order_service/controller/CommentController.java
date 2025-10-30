package com.example.test_order_service.controller;

import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import com.example.test_order_service.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/test-orders/{orderId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public RestResponse<CommentResponse> createComment(
            @PathVariable String orderId,
            @RequestBody @Valid CreateCommentRequest request
    ) {
        return commentService.createComment(orderId, request);
    }

    @GetMapping("/all")
    public RestResponse<List<CommentResponse>> getAllComments(
            @PathVariable String orderId
    ) {
        List<CommentResponse> result = commentService.getAllComments(orderId);

        return RestResponse.<List<CommentResponse>>builder()
                .timestamp(LocalDateTime.now())
                .statusCode(200)
                .message("All comments for order " + orderId + " retrieved successfully")
                .result(result)
                .build();
    }

    @PutMapping("/{commentId}")
    public RestResponse<CommentResponse> updateComment(
            @PathVariable String orderId,
            @PathVariable String commentId,
            @RequestBody @Valid UpdateCommentRequest request
    ) {
        return commentService.updateComment(orderId, commentId, request);
    }

    @DeleteMapping("/{commentId}")
    public RestResponse<Void> deleteComment(
            @PathVariable String orderId,
            @PathVariable String commentId
    ) {
        return commentService.deleteComment(orderId, commentId);
    }
}
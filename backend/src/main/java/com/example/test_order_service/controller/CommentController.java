package com.example.test_order_service.controller;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import com.example.test_order_service.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/test-orders/{orderId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('REVIEW_TEST_ORDER')")
    public RestResponse<CommentResponse> createComment(
            @PathVariable String orderId,
            @RequestBody @Valid CreateCommentRequest request
    ) {
        return commentService.createComment(orderId, request);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('ADD_COMMENT')")
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

    @PostMapping("/ai-reviewed")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('ADD_COMMENT')")
    public Mono<RestResponse<CommentResponse>> getAIReview(
            @PathVariable String orderId
    ) {
        return commentService.getAIReview(orderId);
    }

    @PutMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('MODIFY_COMMENT')")
    public RestResponse<CommentResponse> updateComment(
            @PathVariable String orderId,
            @PathVariable String commentId,
            @RequestBody @Valid UpdateCommentRequest request
    ) {
        return commentService.updateComment(orderId, commentId, request);
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("hasAnyRole('LAB_USER', 'ADMIN') and hasAuthority('DELETE_COMMENT')")
    public RestResponse<Void> deleteComment(
            @PathVariable String orderId,
            @PathVariable String commentId
    ) {
        return commentService.deleteComment(orderId, commentId);
    }
}
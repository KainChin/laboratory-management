package com.example.test_order_service.unit.mapper;

import com.example.test_order_service.dto.response.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.mapper.CommentMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommentMapperTest {

    private final CommentMapper commentMapper = Mappers.getMapper(CommentMapper.class);

    @Test
    void toCommentEntity() {
        CreateCommentRequest request = CreateCommentRequest.builder()
                .commentText("This is a test comment.")
                .build();

        Comment comment = commentMapper.toCommentEntity(request);

        assertThat(comment).isNotNull();
        assertThat(comment.getCommentText()).isEqualTo(request.getCommentText());
    }

    @Test
    void toCommentResponse() {
        TestOrder testOrder = TestOrder.builder().testOrderId("order123").build();

        Comment comment = Comment.builder()
                .commentId("comment1")
                .commentText("This is a test comment.")
                .createdBy("testuser")
                .createdAt(LocalDateTime.now())
                .testOrder(testOrder)
                .build();

        CommentResponse response = commentMapper.toCommentResponse(comment);

        assertThat(response).isNotNull();
        assertThat(response.getCommentId()).isEqualTo(comment.getCommentId());
        assertThat(response.getCommentText()).isEqualTo(comment.getCommentText());
        assertThat(response.getCreatedBy()).isEqualTo(comment.getCreatedBy());
        assertThat(response.getCreatedAt()).isEqualTo(comment.getCreatedAt());
    }

    @Test
    void toCommentResponses() {
        TestOrder testOrder = TestOrder.builder().testOrderId("order123").build();

        Comment comment = Comment.builder()
                .commentId("comment1")
                .commentText("This is a test comment.")
                .createdBy("testuser")
                .createdAt(LocalDateTime.now())
                .testOrder(testOrder)
                .build();

        List<Comment> comments = Collections.singletonList(comment);

        List<CommentResponse> responses = commentMapper.toCommentResponses(comments);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getCommentId()).isEqualTo(comment.getCommentId());
        assertThat(responses.get(0).getCommentText()).isEqualTo(comment.getCommentText());
        assertThat(responses.get(0).getCreatedBy()).isEqualTo(comment.getCreatedBy());
        assertThat(responses.get(0).getCreatedAt()).isEqualTo(comment.getCreatedAt());
    }
}

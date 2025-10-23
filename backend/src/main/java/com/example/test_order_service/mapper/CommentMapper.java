package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.repsonse.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    Comment toCommentEntity(CreateCommentRequest request);
    CommentResponse toCommentResponse(Comment comment);
    List<CommentResponse> toCommentResponses(List<Comment> comments);
}
package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.repsonse.CommentResponse;
import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    Comment toCommentEntity(CreateCommentRequest request);

    @Mapping(target = "testOrderId", source = "testOrder.testOrderId")
    CommentResponse toCommentResponse(Comment comment);
}
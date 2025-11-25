package com.example.test_order_service.controller;

import com.example.test_order_service.dto.request.CreateCommentRequest;
import com.example.test_order_service.dto.request.UpdateCommentRequest;
import com.example.test_order_service.dto.response.CommentResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.service.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean CommentService commentService;

    @Test
    void createComment_shouldReturnServiceResponse() throws Exception {
        String orderId = "order-1";

        CreateCommentRequest req = new CreateCommentRequest();
        req.setCommentText("hello");

        CommentResponse commentRes = CommentResponse.builder()
                .commentId("c1")
                .commentText("hello")
                .createdBy("user")
                .createdAt(LocalDateTime.now())
                .build();

        RestResponse<CommentResponse> serviceRes = RestResponse.<CommentResponse>builder()
                .statusCode(200)
                .message("created")
                .result(commentRes)
                .timestamp(LocalDateTime.now())
                .build();

        when(commentService.createComment(eq(orderId), any(CreateCommentRequest.class)))
                .thenReturn(serviceRes);

        ResultActions act = mockMvc.perform(
                MockMvcRequestBuilders.post("/api/test-orders/{orderId}/comments", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
        );

        act.andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.result.commentId").value("c1"))
                .andExpect(jsonPath("$.result.commentText").value("hello"));

        verify(commentService).createComment(eq(orderId), any(CreateCommentRequest.class));
    }

    @Test
    void getAllComments_shouldWrapListIntoRestResponse() throws Exception {
        String orderId = "order-1";

        List<CommentResponse> list = List.of(
                CommentResponse.builder().commentId("c1").commentText("a").build(),
                CommentResponse.builder().commentId("c2").commentText("b").build()
        );

        when(commentService.getAllComments(orderId)).thenReturn(list);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/api/test-orders/{orderId}/comments/all", orderId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.result").isArray())
                .andExpect(jsonPath("$.result.length()").value(2));

        verify(commentService).getAllComments(orderId);
    }

    @Test
    void updateComment_shouldReturnServiceResponse() throws Exception {
        String orderId = "order-1";
        String commentId = "c1";

        UpdateCommentRequest req = new UpdateCommentRequest();
        req.setCommentText("new text");

        CommentResponse commentRes = CommentResponse.builder()
                .commentId("c1")
                .commentText("new text")
                .build();

        RestResponse<CommentResponse> serviceRes = RestResponse.<CommentResponse>builder()
                .statusCode(200)
                .message("updated")
                .result(commentRes)
                .timestamp(LocalDateTime.now())
                .build();

        when(commentService.updateComment(eq(orderId), eq(commentId), any(UpdateCommentRequest.class)))
                .thenReturn(serviceRes);

        mockMvc.perform(
                        MockMvcRequestBuilders.put("/api/test-orders/{orderId}/comments/{commentId}", orderId, commentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.result.commentText").value("new text"));

        verify(commentService).updateComment(eq(orderId), eq(commentId), any(UpdateCommentRequest.class));
    }

    @Test
    void deleteComment_shouldReturnServiceResponse() throws Exception {
        String orderId = "order-1";
        String commentId = "c1";

        RestResponse<Void> serviceRes = RestResponse.<Void>builder()
                .statusCode(200)
                .message("deleted")
                .timestamp(LocalDateTime.now())
                .build();

        when(commentService.deleteComment(orderId, commentId)).thenReturn(serviceRes);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/api/test-orders/{orderId}/comments/{commentId}", orderId, commentId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200));

        verify(commentService).deleteComment(orderId, commentId);
    }
}

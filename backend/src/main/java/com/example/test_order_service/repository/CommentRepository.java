package com.example.test_order_service.repository;

import com.example.test_order_service.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findAllByTestOrder_TestOrderId(String testOrderId);
    Optional<Comment> findByCommentIdAndTestOrder_TestOrderId(String commentId, String testOrderId);
}
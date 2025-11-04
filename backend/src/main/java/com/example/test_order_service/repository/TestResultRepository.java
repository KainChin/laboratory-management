package com.example.test_order_service.repository;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TestResultRepository extends JpaRepository<TestResult, String> {
    Optional<TestResult> findByBloodCollectionId(String bloodCollectionId);
    Optional<TestResult> findByTestOrder(TestOrder testOrder);
}

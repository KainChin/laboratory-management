package com.example.test_order_service.repository;

import com.example.test_order_service.entity.TestResult;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestResultRepository extends JpaRepository<TestResult, String> {
}

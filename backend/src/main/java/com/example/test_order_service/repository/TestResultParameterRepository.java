package com.example.test_order_service.repository;

import com.example.test_order_service.entity.TestResultParameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestResultParameterRepository extends JpaRepository<TestResultParameter, String> {
}

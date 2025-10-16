package com.example.test_order_service.repository;

import com.example.test_order_service.entity.TestOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestOrderRepository extends JpaRepository<TestOrder, String> {
}

package com.example.test_order_service.repository;

import com.example.test_order_service.entity.FlaggingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlaggingConfigRepository extends JpaRepository<FlaggingConfig, String> {
}

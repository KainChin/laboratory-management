package com.example.test_order_service.repository;

import com.example.test_order_service.entity.FlaggingConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FlaggingConfigRepository extends JpaRepository<FlaggingConfig, String> {
    Optional<FlaggingConfig> findByParameterIgnoreCase(String parameter);
}

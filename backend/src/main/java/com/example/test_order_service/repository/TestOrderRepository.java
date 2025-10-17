package com.example.test_order_service.repository;

import com.example.test_order_service.entity.TestOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TestOrderRepository extends JpaRepository<TestOrder, String> {
    @Query("SELECT t FROM TestOrder t where t.patientName ILIKE CONCAT('%', :keyword, '%')")
    Page<TestOrder> findTestOrdersByParams(Pageable pageable, @Param("keyword") String keyword);
}

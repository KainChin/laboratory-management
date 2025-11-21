package com.example.test_order_service.repository;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TestOrderRepository extends JpaRepository<TestOrder, String> {
    @Query("SELECT t FROM TestOrder t WHERE t.deleted = false " +
            "AND t.patientName ILIKE CONCAT('%', :keyword, '%') " +
            "AND (CAST(:startDateTime AS timestamp) IS NULL OR t.createdAt >= :startDateTime) " +
            "AND (CAST(:endDateTime AS timestamp) IS NULL OR t.createdAt <= :endDateTime) " +
            "AND (:status IS NULL OR t.status = :status)")
    Page<TestOrder> findTestOrdersByParams(Pageable pageable,
                                           @Param("keyword") String keyword,
                                           @Param("startDateTime") LocalDateTime startDateTime,
                                           @Param("endDateTime") LocalDateTime endDateTime,
                                           @Param("status") TestOrderStatus status);

    @Query("SELECT COUNT(t) FROM TestOrder t WHERE t.deleted = false")
    long countActive();

    @Query("SELECT t.status AS status, COUNT(t) AS count FROM TestOrder t WHERE t.deleted = false GROUP BY t.status")
    List<Object[]> countByStatus();

    Optional<TestOrder> findByBloodCollectionId(String bloodCollectionId);

    @Query("SELECT count(*) FROM TestOrder t WHERE t.bloodCollectionId LIKE CONCAT('%', :dateCode, '%')")
    long countByDateCode(@Param("dateCode") String dateCode);

    @Query("SELECT t FROM TestOrder t where t.deleted = false AND t.email ILIKE CONCAT('%', :email, '%')") Page<TestOrder> findByEmail(Pageable pageable, @Param("email") String email);

    @Query("SELECT t FROM TestOrder t WHERE t.deleted = false AND t.createdAt >= :startOfWeek AND t.createdAt < :endOfWeek")
    List<TestOrder> findTestOrdersInCurrentWeek(@Param("startOfWeek") LocalDateTime startOfWeek, @Param("endOfWeek") LocalDateTime endOfWeek);
}
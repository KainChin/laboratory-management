package com.example.test_order_service.ingest.service;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter; // <-- THÊM import này
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.ingest.dto.TestResultIngestFullPayload;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.repository.TestResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestResultIngestFullService {
    private final TestResultRepository testResultRepository;
    private final TestOrderRepository testOrderRepository;

    @Transactional
    public void ingest(TestResultIngestFullPayload p) {
        if (p == null) {
            log.warn("Received null payload");
            return;
        }

        // 1. Tìm TestOrder cha (bắt buộc phải có).
        String orderId = p.getTestOrderId();
        TestOrder order = testOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("TestOrder not found: " + orderId));

        // 2. Tìm TestResult hiện có cho TestOrder này, hoặc tạo mới.
        TestResult result = testResultRepository.findByTestOrder(order)
                .orElseGet(() -> {
                    log.info("Không tìm thấy TestResult cho orderId={}, đang tạo mới.", orderId);
                    return TestResult.builder()
                            .testOrder(order)
                            .patientId(order.getPatientId())
                            .bloodCollectionId("N/A") // Cần xác định nguồn cho trường này
                            .instrumentName("N/A")    // Cần xác định nguồn cho trường này
                            .hl7RawData("N/A")        // Cần xác định nguồn cho trường này
                            .status("IN_PROGRESS")
                            .testResultParameter(new ArrayList<>())
                            .build();
                });

        // 3. Tạo đối tượng TestResultParameter con từ payload.
        TestResultParameter newParameter = TestResultParameter.builder()
                // Ánh xạ dữ liệu từ payload (DTO) sang entity
                .testOrder(order)
                .paramName(p.getParameter()) // payload.parameter -> entity.paramName
                .value(p.getValue() != null ? String.valueOf(p.getValue()) : null) // Chuyển Double -> String
                .unit(p.getUnit())
                .refRange(formatRefRange(p.getMinValue(), p.getMaxValue())) // Ghép min/max thành chuỗi
                .flag(p.getFlag() != null ? (p.getFlag() ? "H" : "N") : null) // Chuyển Boolean -> String
                .paramCode(p.getParameter()) // Tạm thời dùng chung, có thể thay đổi sau
                .sequence(1) // Tạm thời là 1, có thể thay đổi sau
                .build();

        // 4. Liên kết cha và con.
        newParameter.setTestResult(result); // Gán cha cho con
        result.getTestResultParameter().add(newParameter); // Thêm con vào danh sách của cha

        // 5. Cập nhật trạng thái tổng thể của TestResult.
        result.setStatus(p.getStatus() == null ? "COMPLETED" : p.getStatus());

        // 6. Lưu TestResult (cha), TestResultParameter (con) sẽ được lưu tự động.
        testResultRepository.save(result);

        log.info("Đã lưu/cập nhật TestResult id={} với tham số mới '{}' cho orderId={}",
                result.getResultId(),
                newParameter.getParamName(),
                orderId);
    }

    /**
     * Hàm hỗ trợ để tạo chuỗi khoảng tham chiếu từ giá trị min và max.
     */
    private String formatRefRange(Double min, Double max) {
        if (min == null && max == null) {
            return null;
        }
        if (min != null && max != null) {
            return min + " - " + max;
        }
        return min != null ? ">= " + min : "<= " + max;
    }
}
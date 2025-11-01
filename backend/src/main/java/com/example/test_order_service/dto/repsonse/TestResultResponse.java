package com.example.test_order_service.dto.repsonse;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResultParameter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TestResultResponse {
    private String bloodCollectionId;
    private String instrumentName;
    private String status;
    private List<TestResultParameter> testResultParameter;
}

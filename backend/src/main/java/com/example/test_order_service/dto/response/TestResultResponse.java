package com.example.test_order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TestResultResponse {
    private String bloodCollectionId;
    private String instrumentName;
    private String status;
    private String hl7RawData;
    private List<TestResultParameterResponse> testResultParameter;
}

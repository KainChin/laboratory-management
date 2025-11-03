package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestResult;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TestResultMapper {
    List<TestResultResponse> toTestResultResponses(List<TestResult> testResults);
    TestResultResponse toTestResultResponse(TestResult testResult);
}

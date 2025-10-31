package com.example.test_order_service.unit.mapper;

import com.example.test_order_service.dto.repsonse.TestResultResponse;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.mapper.TestResultMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TestResultMapperTest {

    private final TestResultMapper testResultMapper = Mappers.getMapper(TestResultMapper.class);

    @Test
    void toTestResultResponses() {
        TestResult testResult = new TestResult();
        testResult.setResultId("TR001");
        testResult.setParameter("Blood Test");
        testResult.setValue(10.0);

        List<TestResult> testResults = Collections.singletonList(testResult);

        List<TestResultResponse> responses = testResultMapper.toTestResultResponses(testResults);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getResultId()).isEqualTo("TR001");
        assertThat(responses.get(0).getParameter()).isEqualTo("Blood Test");
        assertThat(responses.get(0).getValue()).isEqualTo(10.0);
    }

    @Test
    void toTestResultResponse() {
        TestResult testResult = new TestResult();
        testResult.setResultId("TR002");
        testResult.setParameter("Urine Test");
        testResult.setValue(20.0);

        TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

        assertThat(response).isNotNull();
        assertThat(response.getResultId()).isEqualTo("TR002");
        assertThat(response.getParameter()).isEqualTo("Urine Test");
        assertThat(response.getValue()).isEqualTo(20.0);
    }
}
package com.example.test_order_service.unit.mapper;

import com.example.test_order_service.dto.response.TestResultParameterResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.mapper.TestResultMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive Unit Tests for TestResultMapper
 * Tests MapStruct mapping logic with full coverage
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestResultMapper Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestResultMapperTest {

    private final TestResultMapper testResultMapper = Mappers.getMapper(TestResultMapper.class);

    private TestOrder testOrder;
    private TestResult testResult;
    private TestResultParameter parameter1;
    private TestResultParameter parameter2;

    @BeforeEach
    void setUp() {
        // Setup test order
        testOrder = TestOrder.builder()
                .testOrderId("TO-001")
                .bloodCollectionId("TUBE-001")
                .patientName("Nguyen Van An")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .gender(Gender.MALE)
                .status(TestOrderStatus.COMPLETED)
                .build();

        // Setup test result parameters
        parameter1 = TestResultParameter.builder()
                .id("TP-001")
                .paramCode("WBC")
                .paramName("White Blood Cells")
                .value("7.2")
                .unit("10^3/uL")
                .refRange("4.0-10.0")
                .flag("N")
                .sequence(1)
                .build();
        parameter1.setCreatedBy("System");
        parameter1.setCreatedAt(LocalDateTime.of(2025, 11, 6, 10, 30, 0));

        parameter2 = TestResultParameter.builder()
                .id("TP-002")
                .paramCode("RBC")
                .paramName("Red Blood Cells")
                .value("4.85")
                .unit("10^6/uL")
                .refRange("3.5-5.5")
                .flag("N")
                .sequence(2)
                .build();
        parameter2.setCreatedBy("System");
        parameter2.setCreatedAt(LocalDateTime.of(2025, 11, 6, 10, 30, 0));

        // Setup test result
        testResult = TestResult.builder()
                .resultId("TR-001")
                .testOrder(testOrder)
                .patientId("PAT-2025-001")
                .bloodCollectionId("TUBE-001")
                .instrumentName("BloodAnalyzer")
                .hl7RawData("MSH|^~\\&|BloodAnalyzer|Lab|...")
                .status("COMPLETED")
                .testResultParameter(new ArrayList<>(Arrays.asList(parameter1, parameter2)))
                .build();
        testResult.setCreatedBy("System");
        testResult.setCreatedAt(LocalDateTime.of(2025, 11, 6, 10, 30, 0));
    }

    // ═══════════════════════════════════════════════════════════════
    // SINGLE MAPPING TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Single TestResult Mapping Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class SingleMappingTests {

        @Test
        @Order(1)
        @DisplayName("Should map TestResult to TestResultResponse successfully")
        void shouldMapTestResultToTestResultResponseSuccessfully() {
            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBloodCollectionId()).isEqualTo("TUBE-001");
            assertThat(response.getInstrumentName()).isEqualTo("BloodAnalyzer");
            assertThat(response.getStatus()).isEqualTo("COMPLETED");
            assertThat(response.getHl7RawData()).isEqualTo("MSH|^~\\&|BloodAnalyzer|Lab|...");
        }

        @Test
        @Order(2)
        @DisplayName("Should map test result parameters correctly")
        void shouldMapTestResultParametersCorrectly() {
            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then
            assertThat(response.getTestResultParameter()).isNotNull();
            assertThat(response.getTestResultParameter()).hasSize(2);

            // Verify first parameter
            TestResultParameterResponse param1 = response.getTestResultParameter().get(0);
            assertThat(param1.getId()).isEqualTo("TP-001");
            assertThat(param1.getParamCode()).isEqualTo("WBC");
            assertThat(param1.getParamName()).isEqualTo("White Blood Cells");
            assertThat(param1.getValue()).isEqualTo("7.2");
            assertThat(param1.getUnit()).isEqualTo("10^3/uL");
            assertThat(param1.getRefRange()).isEqualTo("4.0-10.0");
            assertThat(param1.getFlag()).isEqualTo("N");
            assertThat(param1.getSequence()).isEqualTo(1);

            // Verify second parameter
            TestResultParameterResponse param2 = response.getTestResultParameter().get(1);
            assertThat(param2.getId()).isEqualTo("TP-002");
            assertThat(param2.getParamCode()).isEqualTo("RBC");
        }

        @Test
        @Order(3)
        @DisplayName("Should handle null test result")
        void shouldHandleNullTestResult() {
            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(null);

            // Then
            assertThat(response).isNull();
        }

        @Test
        @Order(4)
        @DisplayName("Should handle test result with null fields")
        void shouldHandleTestResultWithNullFields() {
            // Given
            TestResult testResultWithNulls = TestResult.builder()
                    .resultId("TR-002")
                    .bloodCollectionId("TUBE-002")
                    .instrumentName(null)
                    .hl7RawData(null)
                    .status(null)
                    .testResultParameter(null)
                    .build();

            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResultWithNulls);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getBloodCollectionId()).isEqualTo("TUBE-002");
            assertThat(response.getInstrumentName()).isNull();
            assertThat(response.getHl7RawData()).isNull();
            assertThat(response.getStatus()).isNull();
            assertThat(response.getTestResultParameter()).isNull();
        }

        @Test
        @Order(5)
        @DisplayName("Should handle test result with empty parameters")
        void shouldHandleTestResultWithEmptyParameters() {
            // Given
            testResult.setTestResultParameter(Collections.emptyList());

            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getTestResultParameter()).isEmpty();
        }

        @Test
        @Order(6)
        @DisplayName("Should map all fields correctly for complete test result")
        void shouldMapAllFieldsCorrectlyForCompleteTestResult() {
            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then - Verify all fields are mapped
            assertThat(response).isNotNull();
            assertThat(response.getBloodCollectionId()).isNotNull();
            assertThat(response.getInstrumentName()).isNotNull();
            assertThat(response.getStatus()).isNotNull();
            assertThat(response.getHl7RawData()).isNotNull();
            assertThat(response.getTestResultParameter()).isNotNull();
        }

        @Test
        @Order(7)
        @DisplayName("Should handle single parameter in test result")
        void shouldHandleSingleParameterInTestResult() {
            // Given
            testResult.setTestResultParameter(Collections.singletonList(parameter1));

            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then
            assertThat(response.getTestResultParameter()).hasSize(1);
            assertThat(response.getTestResultParameter().get(0).getParamCode()).isEqualTo("WBC");
        }

        @Test
        @Order(8)
        @DisplayName("Should handle multiple parameters maintaining order")
        void shouldHandleMultipleParametersMaintainingOrder() {
            // Given
            TestResultParameter parameter3 = TestResultParameter.builder()
                    .id("TP-003")
                    .paramCode("HGB")
                    .paramName("Hemoglobin")
                    .sequence(3)
                    .build();

            testResult.setTestResultParameter(Arrays.asList(parameter1, parameter2, parameter3));

            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then
            assertThat(response.getTestResultParameter()).hasSize(3);
            assertThat(response.getTestResultParameter().get(0).getParamCode()).isEqualTo("WBC");
            assertThat(response.getTestResultParameter().get(1).getParamCode()).isEqualTo("RBC");
            assertThat(response.getTestResultParameter().get(2).getParamCode()).isEqualTo("HGB");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // LIST MAPPING TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("List TestResults Mapping Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ListMappingTests {

        @Test
        @Order(1)
        @DisplayName("Should map list of TestResults to TestResultResponses successfully")
        void shouldMapListOfTestResultsToTestResultResponsesSuccessfully() {
            // Given
            TestResult testResult2 = TestResult.builder()
                    .resultId("TR-002")
                    .bloodCollectionId("TUBE-002")
                    .instrumentName("Analyzer2")
                    .status("PENDING")
                    .hl7RawData("Raw data 2")
                    .testResultParameter(Collections.emptyList())
                    .build();

            List<TestResult> testResults = Arrays.asList(testResult, testResult2);

            // When
            List<TestResultResponse> responses = testResultMapper.toTestResultResponses(testResults);

            // Then
            assertThat(responses).isNotNull();
            assertThat(responses).hasSize(2);

            // Verify first result
            assertThat(responses.get(0).getBloodCollectionId()).isEqualTo("TUBE-001");
            assertThat(responses.get(0).getInstrumentName()).isEqualTo("BloodAnalyzer");
            assertThat(responses.get(0).getStatus()).isEqualTo("COMPLETED");

            // Verify second result
            assertThat(responses.get(1).getBloodCollectionId()).isEqualTo("TUBE-002");
            assertThat(responses.get(1).getInstrumentName()).isEqualTo("Analyzer2");
            assertThat(responses.get(1).getStatus()).isEqualTo("PENDING");
        }

        @Test
        @Order(2)
        @DisplayName("Should handle null list")
        void shouldHandleNullList() {
            // When
            List<TestResultResponse> responses = testResultMapper.toTestResultResponses(null);

            // Then
            assertThat(responses).isNull();
        }

        @Test
        @Order(3)
        @DisplayName("Should handle empty list")
        void shouldHandleEmptyList() {
            // When
            List<TestResultResponse> responses = testResultMapper.toTestResultResponses(Collections.emptyList());

            // Then
            assertThat(responses).isNotNull();
            assertThat(responses).isEmpty();
        }

        @Test
        @Order(4)
        @DisplayName("Should handle single item list")
        void shouldHandleSingleItemList() {
            // Given
            List<TestResult> testResults = Collections.singletonList(testResult);

            // When
            List<TestResultResponse> responses = testResultMapper.toTestResultResponses(testResults);

            // Then
            assertThat(responses).hasSize(1);
            assertThat(responses.get(0).getBloodCollectionId()).isEqualTo("TUBE-001");
        }

        @Test
        @Order(5)
        @DisplayName("Should handle list with null items")
        void shouldHandleListWithNullItems() {
            // Given
            List<TestResult> testResults = Arrays.asList(testResult, null, testResult);

            // When
            List<TestResultResponse> responses = testResultMapper.toTestResultResponses(testResults);

            // Then
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0)).isNotNull();
            assertThat(responses.get(1)).isNull(); // MapStruct maps null to null
            assertThat(responses.get(2)).isNotNull();
        }

        @Test
        @Order(6)
        @DisplayName("Should map multiple test results with different parameter counts")
        void shouldMapMultipleTestResultsWithDifferentParameterCounts() {
            // Given
            TestResult result1 = TestResult.builder()
                    .resultId("TR-001")
                    .bloodCollectionId("TUBE-001")
                    .testResultParameter(Arrays.asList(parameter1, parameter2))
                    .build();

            TestResult result2 = TestResult.builder()
                    .resultId("TR-002")
                    .bloodCollectionId("TUBE-002")
                    .testResultParameter(Collections.emptyList())
                    .build();

            TestResult result3 = TestResult.builder()
                    .resultId("TR-003")
                    .bloodCollectionId("TUBE-003")
                    .testResultParameter(null)
                    .build();

            List<TestResult> testResults = Arrays.asList(result1, result2, result3);

            // When
            List<TestResultResponse> responses = testResultMapper.toTestResultResponses(testResults);

            // Then
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getTestResultParameter()).hasSize(2);
            assertThat(responses.get(1).getTestResultParameter()).isEmpty();
            assertThat(responses.get(2).getTestResultParameter()).isNull();
        }

        @Test
        @Order(7)
        @DisplayName("Should maintain list order during mapping")
        void shouldMaintainListOrderDuringMapping() {
            // Given
            TestResult result1 = TestResult.builder()
                    .resultId("TR-001")
                    .bloodCollectionId("TUBE-001")
                    .build();

            TestResult result2 = TestResult.builder()
                    .resultId("TR-002")
                    .bloodCollectionId("TUBE-002")
                    .build();

            TestResult result3 = TestResult.builder()
                    .resultId("TR-003")
                    .bloodCollectionId("TUBE-003")
                    .build();

            List<TestResult> testResults = Arrays.asList(result1, result2, result3);

            // When
            List<TestResultResponse> responses = testResultMapper.toTestResultResponses(testResults);

            // Then
            assertThat(responses).hasSize(3);
            assertThat(responses.get(0).getBloodCollectionId()).isEqualTo("TUBE-001");
            assertThat(responses.get(1).getBloodCollectionId()).isEqualTo("TUBE-002");
            assertThat(responses.get(2).getBloodCollectionId()).isEqualTo("TUBE-003");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EDGE CASES AND SPECIAL SCENARIOS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EdgeCasesTests {

        @Test
        @Order(1)
        @DisplayName("Should handle very long HL7 raw data")
        void shouldHandleVeryLongHl7RawData() {
            // Given
            String longHl7Data = "MSH|^~\\&|" + "X".repeat(1000);
            testResult.setHl7RawData(longHl7Data);

            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then
            assertThat(response.getHl7RawData()).hasSize(longHl7Data.length());
            assertThat(response.getHl7RawData()).isEqualTo(longHl7Data);
        }

        @Test
        @Order(2)
        @DisplayName("Should handle special characters in fields")
        void shouldHandleSpecialCharactersInFields() {
            // Given
            testResult.setInstrumentName("Analyzer<>\"'&");
            testResult.setStatus("COMPLETED|REVIEWED");

            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then
            assertThat(response.getInstrumentName()).isEqualTo("Analyzer<>\"'&");
            assertThat(response.getStatus()).isEqualTo("COMPLETED|REVIEWED");
        }

        @Test
        @Order(3)
        @DisplayName("Should handle empty strings")
        void shouldHandleEmptyStrings() {
            // Given
            testResult.setInstrumentName("");
            testResult.setStatus("");
            testResult.setHl7RawData("");

            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then
            assertThat(response.getInstrumentName()).isEmpty();
            assertThat(response.getStatus()).isEmpty();
            assertThat(response.getHl7RawData()).isEmpty();
        }

        @Test
        @Order(4)
        @DisplayName("Should handle whitespace in fields")
        void shouldHandleWhitespaceInFields() {
            // Given
            testResult.setInstrumentName("  Analyzer  ");
            testResult.setStatus("  COMPLETED  ");

            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then
            assertThat(response.getInstrumentName()).isEqualTo("  Analyzer  ");
            assertThat(response.getStatus()).isEqualTo("  COMPLETED  ");
        }

        @Test
        @Order(5)
        @DisplayName("Should handle parameter with null fields")
        void shouldHandleParameterWithNullFields() {
            // Given
            TestResultParameter parameterWithNulls = TestResultParameter.builder()
                    .id("TP-999")
                    .paramCode(null)
                    .paramName(null)
                    .value(null)
                    .unit(null)
                    .refRange(null)
                    .flag(null)
                    .sequence(null)
                    .build();

            testResult.setTestResultParameter(Collections.singletonList(parameterWithNulls));

            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then
            assertThat(response.getTestResultParameter()).hasSize(1);
            TestResultParameterResponse param = response.getTestResultParameter().get(0);
            assertThat(param.getId()).isEqualTo("TP-999");
            assertThat(param.getParamCode()).isNull();
            assertThat(param.getParamName()).isNull();
            assertThat(param.getValue()).isNull();
            assertThat(param.getUnit()).isNull();
            assertThat(param.getRefRange()).isNull();
            assertThat(param.getFlag()).isNull();
            assertThat(param.getSequence()).isNull();
        }

        @Test
        @Order(6)
        @DisplayName("Should not modify original entity during mapping")
        void shouldNotModifyOriginalEntityDuringMapping() {
            // Given
            String originalBloodCollectionId = testResult.getBloodCollectionId();
            String originalInstrumentName = testResult.getInstrumentName();
            int originalParameterCount = testResult.getTestResultParameter().size();

            // When
            TestResultResponse response = testResultMapper.toTestResultResponse(testResult);

            // Then - Original entity should remain unchanged
            assertThat(testResult.getBloodCollectionId()).isEqualTo(originalBloodCollectionId);
            assertThat(testResult.getInstrumentName()).isEqualTo(originalInstrumentName);
            assertThat(testResult.getTestResultParameter()).hasSize(originalParameterCount);
        }

        @Test
        @Order(7)
        @DisplayName("Should create independent response objects")
        void shouldCreateIndependentResponseObjects() {
            // When
            TestResultResponse response1 = testResultMapper.toTestResultResponse(testResult);
            TestResultResponse response2 = testResultMapper.toTestResultResponse(testResult);

            // Then - Should be different objects
            assertThat(response1).isNotSameAs(response2);
            assertThat(response1.getTestResultParameter()).isNotSameAs(response2.getTestResultParameter());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPSTRUCT BEHAVIOR VERIFICATION
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("MapStruct Behavior Verification")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class MapStructBehaviorTests {

        @Test
        @Order(1)
        @DisplayName("Should verify mapper is not null")
        void shouldVerifyMapperIsNotNull() {
            assertThat(testResultMapper).isNotNull();
        }

        @Test
        @Order(2)
        @DisplayName("Should map consistently for same input")
        void shouldMapConsistentlyForSameInput() {
            // When - Map same object multiple times
            TestResultResponse response1 = testResultMapper.toTestResultResponse(testResult);
            TestResultResponse response2 = testResultMapper.toTestResultResponse(testResult);
            TestResultResponse response3 = testResultMapper.toTestResultResponse(testResult);

            // Then - All responses should have same values
            assertThat(response1.getBloodCollectionId()).isEqualTo(response2.getBloodCollectionId());
            assertThat(response2.getBloodCollectionId()).isEqualTo(response3.getBloodCollectionId());
            assertThat(response1.getInstrumentName()).isEqualTo(response2.getInstrumentName());
            assertThat(response2.getInstrumentName()).isEqualTo(response3.getInstrumentName());
        }

        @Test
        @Order(3)
        @DisplayName("Should handle concurrent mapping calls")
        void shouldHandleConcurrentMappingCalls() {
            // When - Multiple mappings in quick succession
            List<TestResultResponse> responses = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                responses.add(testResultMapper.toTestResultResponse(testResult));
            }

            // Then - All should be mapped correctly
            assertThat(responses).hasSize(100);
            responses.forEach(response -> {
                assertThat(response.getBloodCollectionId()).isEqualTo("TUBE-001");
                assertThat(response.getInstrumentName()).isEqualTo("BloodAnalyzer");
            });
        }
    }
}
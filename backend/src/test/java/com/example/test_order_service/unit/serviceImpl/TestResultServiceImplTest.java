package com.example.test_order_service.unit.serviceImpl;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestResultParameterResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.mapper.TestResultMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.repository.TestResultRepository;
import com.example.test_order_service.serviceImpl.TestResultServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for TestResultServiceImpl
 * Tests business logic for HL7 message processing and test result management
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestResultServiceImpl Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestResultServiceImplTest {

    @Mock
    private TestResultRepository testResultRepository;

    @Mock
    private TestOrderRepository testOrderRepository;

    @Mock
    private TestResultMapper testResultMapper;

    @InjectMocks
    private TestResultServiceImpl testResultService;

    private TestOrder testOrder;
    private TestResult testResult;
    private TestResultResponse testResultResponse;
    private String validHl7Message;

    @BeforeEach
    void setUp() {
        // Setup test order - using TUBE-001 as blood collection ID from user's HL7 sample
        testOrder = TestOrder.builder()
                .testOrderId("TO-001")
                .bloodCollectionId("TUBE-001")
                .patientName("Nguyen Van An")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .status(TestOrderStatus.PENDING)
                .build();

        // Setup test result
        testResult = TestResult.builder()
                .resultId("TR-001")
                .testOrder(testOrder)
                .bloodCollectionId("TUBE-001")
                .instrumentName("BloodAnalyzer")
                .status("COMPLETED")
                .hl7RawData("MSH|^~\\&|BloodAnalyzer|Lab|...")
                .build();

        // Setup test result response
        testResultResponse = TestResultResponse.builder()
                .bloodCollectionId("TUBE-001")
                .instrumentName("BloodAnalyzer")
                .status("COMPLETED")
                .hl7RawData("MSH|^~\\&|BloodAnalyzer|Lab|...")
                .testResultParameter(new ArrayList<>())
                .build();

        // Valid HL7 message for testing - from user's actual sample
        validHl7Message = """
                MSH|^~\\&|BloodAnalyzer|Lab|LIS|Hospital|20251103093000||ORU^R01|MSG20251103093000001|P|2.5.1
                PID|1||PAT-2025-001||Nguyen^Van An||19850315|Male
                OBR|1|TUBE-001|CBC^Complete Blood Count|||20251103093000
                OBX|1|NM|WBC^White Blood Cells||7.2|10^3/uL|4.0-10.0|N|||F
                OBX|2|NM|LYM%^Lymphocytes %||28.5|%|20.0-40.0|N|||F
                OBX|3|NM|MID%^Monocytes %||6.8|%|3.0-15.0|N|||F
                OBX|4|NM|GRAN%^Granulocytes %||64.7|%|50.0-70.0|N|||F
                OBX|5|NM|HGB^Hemoglobin||145|g/dL|110.0-160.0|N|||F
                OBX|6|NM|RBC^Red Blood Cells||4.85|10^6/uL|3.5-5.5|N|||F
                OBX|7|NM|HCT^Hematocrit||44.2|%|37.0-54.0|N|||F
                OBX|8|NM|MCV^Mean Corpuscular Volume||91.2|fL|80.0-100.0|N|||F
                OBX|9|NM|MCH^Mean Corpuscular Hemoglobin||29.9|pg|27.0-34.0|N|||F
                OBX|10|NM|MCHC^Mean Corpuscular Hemoglobin Concentration||328|g/L|320.0-360.0|N|||F
                OBX|11|NM|PLT^Platelets||268|10^9/L|150.0-400.0|N|||F
                """;
    }

    // ═══════════════════════════════════════════════════════════════
    // GET RESULT BY BLOOD COLLECTION ID TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Result By Blood Collection ID Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetResultByBloodCollectionIdTests {

        @Test
        @Order(1)
        @DisplayName("Should get test result successfully by blood collection ID")
        void shouldGetTestResultSuccessfully() {
            // Given
            String bloodCollectionId = "TUBE-001";
            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult));
            when(testResultMapper.toTestResultResponse(testResult)).thenReturn(testResultResponse);

            // When
            RestResponse<?> response = testResultService.getResultByBloodCollectionId(bloodCollectionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Test result retrivived successfully");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getTimestamp()).isNotNull();

            verify(testResultRepository).findByBloodCollectionId(bloodCollectionId);
            verify(testResultMapper).toTestResultResponse(testResult);
        }

        @Test
        @Order(2)
        @DisplayName("Should throw exception when blood collection ID not found")
        void shouldThrowExceptionWhenBloodCollectionIdNotFound() {
            // Given
            String invalidId = "INVALID-ID";
            when(testResultRepository.findByBloodCollectionId(invalidId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.getResultByBloodCollectionId(invalidId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Test result not found for blood collection Id: " + invalidId);

            verify(testResultRepository).findByBloodCollectionId(invalidId);
            verify(testResultMapper, never()).toTestResultResponse(any());
        }

        @Test
        @Order(3)
        @DisplayName("Should handle null blood collection ID")
        void shouldHandleNullBloodCollectionId() {
            // Given
            when(testResultRepository.findByBloodCollectionId(null))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.getResultByBloodCollectionId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Test result not found for blood collection Id: null");

            verify(testResultRepository).findByBloodCollectionId(null);
        }

        @Test
        @Order(4)
        @DisplayName("Should handle empty blood collection ID")
        void shouldHandleEmptyBloodCollectionId() {
            // Given
            String emptyId = "";
            when(testResultRepository.findByBloodCollectionId(emptyId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.getResultByBloodCollectionId(emptyId))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(testResultRepository).findByBloodCollectionId(emptyId);
        }

        @Test
        @Order(5)
        @DisplayName("Should handle repository exception")
        void shouldHandleRepositoryException() {
            // Given
            String bloodCollectionId = "TUBE-001";
            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> testResultService.getResultByBloodCollectionId(bloodCollectionId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");

            verify(testResultRepository).findByBloodCollectionId(bloodCollectionId);
        }

        @Test
        @Order(6)
        @DisplayName("Should handle mapper exception")
        void shouldHandleMapperException() {
            // Given
            String bloodCollectionId = "TUBE-001";
            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult));
            when(testResultMapper.toTestResultResponse(testResult))
                    .thenThrow(new RuntimeException("Mapping error"));

            // When & Then
            assertThatThrownBy(() -> testResultService.getResultByBloodCollectionId(bloodCollectionId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Mapping error");

            verify(testResultMapper).toTestResultResponse(testResult);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // RECEIVE HL7 MESSAGE TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Receive HL7 Message Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReceiveHl7Tests {

        @Test
        @Order(1)
        @DisplayName("Should process valid HL7 message successfully")
        void shouldProcessValidHl7MessageSuccessfully() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            RestResponse<TestResultResponse> response = testResultService.receiveHl7(validHl7Message);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("HL7 data processed successfully");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getTimestamp()).isNotNull();

            verify(testResultRepository).findByBloodCollectionId("TUBE-001");
            verify(testOrderRepository).findByBloodCollectionId("TUBE-001");
            verify(testResultRepository).save(any(TestResult.class));
            verify(testOrderRepository).save(argThat(order ->
                    order.getStatus() == TestOrderStatus.COMPLETED
            ));
        }

        @Test
        @Order(2)
        @DisplayName("Should extract blood collection ID from OBR segment")
        void shouldExtractBloodCollectionIdFromOBR() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(validHl7Message);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResult savedResult = captor.getValue();
            assertThat(savedResult.getBloodCollectionId()).isEqualTo("TUBE-001");
        }

        @Test
        @Order(3)
        @DisplayName("Should extract instrument name from MSH segment")
        void shouldExtractInstrumentNameFromMSH() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(validHl7Message);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResult savedResult = captor.getValue();
            assertThat(savedResult.getInstrumentName()).isEqualTo("BloodAnalyzer");
        }

        @Test
        @Order(4)
        @DisplayName("Should parse OBX segments and create test result parameters")
        void shouldParseOBXSegmentsAndCreateParameters() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(validHl7Message);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResult savedResult = captor.getValue();
            assertThat(savedResult.getTestResultParameter()).isNotEmpty();
            assertThat(savedResult.getTestResultParameter()).hasSize(11); // 11 OBX segments in user's sample

            // Verify first parameter (WBC) - from user's actual HL7 sample
            TestResultParameter wbc = savedResult.getTestResultParameter().get(0);
            assertThat(wbc.getParamCode()).isEqualTo("WBC");
            assertThat(wbc.getParamName()).isEqualTo("White Blood Cells");
            assertThat(wbc.getValue()).isEqualTo("7.2");
            assertThat(wbc.getUnit()).isEqualTo("10^3/uL");
            assertThat(wbc.getRefRange()).isEqualTo("4.0-10.0");
            assertThat(wbc.getFlag()).isEqualTo("N");
        }

        @Test
        @Order(5)
        @DisplayName("Should set test result status to COMPLETED")
        void shouldSetTestResultStatusToCompleted() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(validHl7Message);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResult savedResult = captor.getValue();
            assertThat(savedResult.getStatus()).isEqualTo("COMPLETED");
        }

        @Test
        @Order(6)
        @DisplayName("Should update test order status to COMPLETED")
        void shouldUpdateTestOrderStatusToCompleted() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(validHl7Message);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    order.getStatus() == TestOrderStatus.COMPLETED
            ));
        }

        @Test
        @Order(7)
        @DisplayName("Should throw exception when HL7 message is null")
        void shouldThrowExceptionWhenHl7IsNull() {
            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("HL7 message is empty");

            verify(testResultRepository, never()).save(any());
            verify(testOrderRepository, never()).save(any());
        }

        @Test
        @Order(8)
        @DisplayName("Should throw exception when HL7 message is blank")
        void shouldThrowExceptionWhenHl7IsBlank() {
            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("HL7 message is empty");

            verify(testResultRepository, never()).save(any());
        }

        @Test
        @Order(9)
        @DisplayName("Should throw exception when HL7 message is empty")
        void shouldThrowExceptionWhenHl7IsEmpty() {
            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("HL7 message is empty");

            verify(testResultRepository, never()).save(any());
        }

        @Test
        @Order(10)
        @DisplayName("Should throw exception when MSH segment is missing")
        void shouldThrowExceptionWhenMSHMissing() {
            // Given
            String invalidHl7 = """
                    OBR|1|TUBE-001|CBC^Complete Blood Count
                    OBX|1|NM|WBC^White Blood Cell||7.5|10^3/uL|4.0-10.0|N||F
                    """;

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(invalidHl7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid HL7 format: Message must start with MSH segment");
        }

        @Test
        @Order(11)
        @DisplayName("Should throw exception when OBR segment is missing")
        void shouldThrowExceptionWhenOBRMissing() {
            // Given
            String invalidHl7 = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab|LIS|Hospital|20241106103045||ORU^R01|MSG001|P|2.5
                    OBX|1|NM|WBC^White Blood Cell||7.5|10^3/uL|4.0-10.0|N||F
                    """;

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(invalidHl7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid HL7 format: Missing OBR segment");
        }

        @Test
        @Order(12)
        @DisplayName("Should throw exception when OBX segment is missing")
        void shouldThrowExceptionWhenOBXMissing() {
            // Given
            String invalidHl7 = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab|LIS|Hospital|20241106103045||ORU^R01|MSG001|P|2.5
                    OBR|1|TUBE-001|CBC^Complete Blood Count
                    """;

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(invalidHl7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid HL7 format: Missing OBX segment");
        }

        @Test
        @Order(13)
        @DisplayName("Should throw exception when blood collection ID is missing in OBR")
        void shouldThrowExceptionWhenBloodCollectionIdMissingInOBR() {
            // Given - OBR-2 is empty
            String invalidHl7 = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab|LIS|Hospital|20241106103045||ORU^R01|MSG001|P|2.5
                    OBR|1|||CBC^Complete Blood Count
                    OBX|1|NM|WBC^White Blood Cell||7.5|10^3/uL|4.0-10.0|N||F
                    """;

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(invalidHl7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Missing blood collection ID in OBR-2");
        }

        @Test
        @Order(14)
        @DisplayName("Should throw exception when test result already exists for blood collection ID")
        void shouldThrowExceptionWhenTestResultAlreadyExists() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testResult));

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(validHl7Message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TestResult already exists for bloodCollectionId: TUBE-001");

            verify(testResultRepository, never()).save(any());
        }

        @Test
        @Order(15)
        @DisplayName("Should throw exception when test order not found for blood collection ID")
        void shouldThrowExceptionWhenTestOrderNotFound() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(validHl7Message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TestOrder not found for blood collection Id: TUBE-001");

            verify(testResultRepository, never()).save(any());
        }

        @Test
        @Order(16)
        @DisplayName("Should throw exception when MSH has insufficient fields")
        void shouldThrowExceptionWhenMSHHasInsufficientFields() {
            // Given
            String invalidHl7 = """
                    MSH|^
                    OBR|1|TUBE-001|CBC
                    OBX|1|NM|WBC||7.5
                    """;

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(invalidHl7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid HL7 format: MSH segment has insufficient fields");
        }

        @Test
        @Order(17)
        @DisplayName("Should throw exception when encoding characters are missing in MSH-2")
        void shouldThrowExceptionWhenEncodingCharactersMissing() {
            // Given
            String invalidHl7 = """
                    MSH|^|Sysmex
                    OBR|1|TUBE-001|CBC
                    OBX|1|NM|WBC||7.5
                    """;

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(invalidHl7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid HL7 format: Missing encoding characters in MSH-2");
        }

        @Test
        @Order(18)
        @DisplayName("Should throw exception when segment separators are missing")
        void shouldThrowExceptionWhenSegmentSeparatorsMissing() {
            // Given - Single line without newline
            String invalidHl7 = "MSH|^~\\&|BloodAnalyzer|Hospital Lab";

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(invalidHl7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid HL7 format: Missing segment separators");
        }

        @Test
        @Order(19)
        @DisplayName("Should throw exception when segment is missing field separator")
        void shouldThrowExceptionWhenSegmentMissingFieldSeparator() {
            // Given
            String invalidHl7 = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab
                    INVALID_SEGMENT_WITHOUT_PIPES
                    OBR|1|TUBE-001|CBC
                    """;

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(invalidHl7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid HL7 format: Segment missing field separator");
        }

        @Test
        @Order(20)
        @DisplayName("Should throw exception when segment has invalid identifier")
        void shouldThrowExceptionWhenSegmentHasInvalidIdentifier() {
            // Given
            String invalidHl7 = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab
                    12|invalid|segment
                    OBR|1|TUBE-001|CBC
                    """;

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(invalidHl7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid HL7 format: Invalid segment identifier");
        }

        @Test
        @Order(21)
        @DisplayName("Should handle OBX with minimal fields gracefully")
        void shouldHandleOBXWithMinimalFieldsGracefully() {
            // Given
            String minimalHl7 = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab|LIS|Hospital|20241106103045||ORU^R01|MSG001|P|2.5
                    OBR|1|TUBE-001|CBC^Complete Blood Count
                    OBX|1|NM|WBC^White Blood Cell
                    """;

            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            RestResponse<TestResultResponse> response = testResultService.receiveHl7(minimalHl7);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(200);

            // Verify that parameters with insufficient fields are skipped
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());
            // OBX with < 6 fields should be skipped
            assertThat(captor.getValue().getTestResultParameter()).isEmpty();
        }

        @Test
        @Order(22)
        @DisplayName("Should handle OBX with single parameter code (no caret)")
        void shouldHandleOBXWithSingleParameterCode() {
            // Given
            String hl7WithSingleCode = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab|LIS|Hospital|20241106103045||ORU^R01|MSG001|P|2.5
                    OBR|1|TUBE-001|CBC^Complete Blood Count
                    OBX|1|NM|WBC||7.5|10^3/uL|4.0-10.0|N||F
                    """;

            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(hl7WithSingleCode);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResultParameter param = captor.getValue().getTestResultParameter().get(0);
            assertThat(param.getParamCode()).isEqualTo("WBC");
            assertThat(param.getParamName()).isEqualTo("WBC"); // Should use code as name
        }

        @Test
        @Order(23)
        @DisplayName("Should parse sequence number from OBX-1")
        void shouldParseSequenceNumberFromOBX() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(validHl7Message);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            List<TestResultParameter> params = captor.getValue().getTestResultParameter();
            assertThat(params.get(0).getSequence()).isEqualTo(1);
            assertThat(params.get(1).getSequence()).isEqualTo(2);
            assertThat(params.get(2).getSequence()).isEqualTo(3);
            assertThat(params.get(3).getSequence()).isEqualTo(4);
        }

        @Test
        @Order(24)
        @DisplayName("Should set computedBy to HL7 Parser v2.0 for all parameters")
        void shouldSetComputedByForAllParameters() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(validHl7Message);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            List<TestResultParameter> params = captor.getValue().getTestResultParameter();
            params.forEach(param ->
                    assertThat(param.getComputedBy()).isEqualTo("HL7 Parser v2.0")
            );
        }

        @Test
        @Order(25)
        @DisplayName("Should link test result parameters to test order and test result")
        void shouldLinkTestResultParametersToTestOrderAndTestResult() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(validHl7Message);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResult savedResult = captor.getValue();
            savedResult.getTestResultParameter().forEach(param -> {
                assertThat(param.getTestResult()).isEqualTo(savedResult);
                assertThat(param.getTestOrder()).isEqualTo(testOrder);
            });
        }

        @Test
        @Order(26)
        @DisplayName("Should handle invalid sequence number gracefully")
        void shouldHandleInvalidSequenceNumberGracefully() {
            // Given
            String hl7WithInvalidSequence = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab|LIS|Hospital|20241106103045||ORU^R01|MSG001|P|2.5
                    OBR|1|TUBE-001|CBC^Complete Blood Count
                    OBX|INVALID|NM|WBC^White Blood Cell||7.5|10^3/uL|4.0-10.0|N||F
                    """;

            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(hl7WithInvalidSequence);

            // Then - Should default to 0
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResultParameter param = captor.getValue().getTestResultParameter().get(0);
            assertThat(param.getSequence()).isEqualTo(0);
        }

        @Test
        @Order(27)
        @DisplayName("Should ignore blank lines in HL7 message")
        void shouldIgnoreBlankLinesInHl7Message() {
            // Given
            String hl7WithBlankLines = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab|LIS|Hospital|20241106103045||ORU^R01|MSG001|P|2.5

                    OBR|1|TUBE-001|CBC^Complete Blood Count

                    OBX|1|NM|WBC^White Blood Cell||7.5|10^3/uL|4.0-10.0|N||F
                    """;

            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            RestResponse<TestResultResponse> response = testResultService.receiveHl7(hl7WithBlankLines);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(200);
            verify(testResultRepository).save(any(TestResult.class));
        }

        @Test
        @Order(28)
        @DisplayName("Should ignore unknown segment types (e.g., PID)")
        void shouldIgnoreUnknownSegmentTypes() {
            // Given - PID segment should be ignored
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When - validHl7Message contains PID segment
            RestResponse<TestResultResponse> response = testResultService.receiveHl7(validHl7Message);

            // Then - Should process successfully despite PID segment
            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @Order(29)
        @DisplayName("Should store raw HL7 data in test result")
        void shouldStoreRawHl7DataInTestResult() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(validHl7Message);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResult savedResult = captor.getValue();
            assertThat(savedResult.getHl7RawData()).isEqualTo(validHl7Message);
        }

        @Test
        @Order(30)
        @DisplayName("Should trim blood collection ID before processing")
        void shouldTrimBloodCollectionIdBeforeProcessing() {
            // Given
            String hl7WithSpaces = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab|LIS|Hospital|20241106103045||ORU^R01|MSG001|P|2.5
                    OBR|1|  TUBE-001  |CBC^Complete Blood Count
                    OBX|1|NM|WBC^White Blood Cell||7.5|10^3/uL|4.0-10.0|N||F
                    """;

            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(hl7WithSpaces);

            // Then
            verify(testOrderRepository).findByBloodCollectionId("TUBE-001");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // REPUBLISH TEST RESULT EVENT TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Republish Test Result Event Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RepublishTestResultEventTests {

        @Test
        @Order(1)
        @DisplayName("Should return not implemented response")
        void shouldReturnNotImplementedResponse() {
            // When
            RestResponse<Void> response = testResultService.republishTestResultEvent("TO-001");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(501);
            assertThat(response.getMessage()).isEqualTo("Republishing events is not supported in this service implementation.");
            assertThat(response.getError()).isEqualTo("Not Implemented");
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getResult()).isNull();
        }

        @Test
        @Order(2)
        @DisplayName("Should return not implemented for any test order ID")
        void shouldReturnNotImplementedForAnyTestOrderId() {
            // When
            RestResponse<Void> response1 = testResultService.republishTestResultEvent("TO-001");
            RestResponse<Void> response2 = testResultService.republishTestResultEvent("TO-999");
            RestResponse<Void> response3 = testResultService.republishTestResultEvent(null);

            // Then
            assertThat(response1.getStatusCode()).isEqualTo(501);
            assertThat(response2.getStatusCode()).isEqualTo(501);
            assertThat(response3.getStatusCode()).isEqualTo(501);
        }

        @Test
        @Order(3)
        @DisplayName("Should not call any repository methods")
        void shouldNotCallAnyRepositoryMethods() {
            // When
            testResultService.republishTestResultEvent("TO-001");

            // Then
            verify(testResultRepository, never()).findById(anyString());
            verify(testResultRepository, never()).save(any());
            verify(testOrderRepository, never()).findById(anyString());
            verify(testOrderRepository, never()).save(any());
        }
    }
}




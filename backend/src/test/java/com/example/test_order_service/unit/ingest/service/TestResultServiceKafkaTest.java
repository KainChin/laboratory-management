package com.example.test_order_service.unit.ingest.service;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.ingest.publisher.TestResultEventPublisher;
import com.example.test_order_service.ingest.service.TestResultServiceKafka;
import com.example.test_order_service.mapper.TestResultMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.repository.TestResultRepository;
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
 * Unit Tests for TestResultServiceKafka
 * Tests business logic for HL7 message processing, event publishing, and test result management
 * with Kafka integration
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestResultServiceKafka Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestResultServiceKafkaTest {

    @Mock
    private TestResultRepository testResultRepository;

    @Mock
    private TestOrderRepository testOrderRepository;

    @Mock
    private TestResultMapper testResultMapper;

    @Mock
    private TestResultEventPublisher eventPublisher;

    @InjectMocks
    private TestResultServiceKafka testResultService;

    private TestOrder testOrder;
    private TestResult testResult;
    private TestResultResponse testResultResponse;
    private String validHl7Message;

    @BeforeEach
    void setUp() {
        // Setup self-reference for transaction proxy support
        testResultService.setSelf(testResultService);

        // Setup test order - using TUBE-001 as blood collection ID
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
                .testResultParameter(new ArrayList<>())
                .build();

        // Setup test result response
        testResultResponse = TestResultResponse.builder()
                .bloodCollectionId("TUBE-001")
                .instrumentName("BloodAnalyzer")
                .status("COMPLETED")
                .hl7RawData("MSH|^~\\&|BloodAnalyzer|Lab|...")
                .testResultParameter(new ArrayList<>())
                .build();

        // Valid HL7 message for testing
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
            assertThat(response.getMessage()).isEqualTo("Retrieved test result successfully");
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
            verify(eventPublisher).publishTestResultCreated(any(TestResult.class));
        }

        @Test
        @Order(2)
        @DisplayName("Should publish event after successful HL7 processing")
        void shouldPublishEventAfterSuccessfulProcessing() {
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
            verify(eventPublisher).publishTestResultCreated(captor.capture());
            assertThat(captor.getValue()).isNotNull();
        }

        @Test
        @Order(3)
        @DisplayName("Should not publish event when HL7 processing fails")
        void shouldNotPublishEventWhenProcessingFails() {
            // Given
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(validHl7Message))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(eventPublisher, never()).publishTestResultCreated(any());
        }

        @Test
        @Order(4)
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
        @Order(5)
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
        @Order(6)
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
            assertThat(savedResult.getTestResultParameter()).hasSize(11);

            // Verify first parameter (WBC)
            TestResultParameter wbc = savedResult.getTestResultParameter().get(0);
            assertThat(wbc.getParamCode()).isEqualTo("WBC");
            assertThat(wbc.getParamName()).isEqualTo("White Blood Cells");
            assertThat(wbc.getValue()).isEqualTo("7.2");
            assertThat(wbc.getUnit()).isEqualTo("10^3/uL");
            assertThat(wbc.getRefRange()).isEqualTo("4.0-10.0");
            assertThat(wbc.getFlag()).isEqualTo("N");
        }

        @Test
        @Order(7)
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
        @Order(8)
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
        @Order(9)
        @DisplayName("Should throw exception when HL7 message is null")
        void shouldThrowExceptionWhenHl7IsNull() {
            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("HL7 message is empty");

            verify(testResultRepository, never()).save(any());
            verify(testOrderRepository, never()).save(any());
            verify(eventPublisher, never()).publishTestResultCreated(any());
        }

        @Test
        @Order(10)
        @DisplayName("Should throw exception when HL7 message is blank")
        void shouldThrowExceptionWhenHl7IsBlank() {
            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("HL7 message is empty");

            verify(testResultRepository, never()).save(any());
        }

        @Test
        @Order(11)
        @DisplayName("Should throw exception when HL7 message is empty")
        void shouldThrowExceptionWhenHl7IsEmpty() {
            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(""))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("HL7 message is empty");

            verify(testResultRepository, never()).save(any());
        }

        @Test
        @Order(12)
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
        @Order(13)
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
        @Order(14)
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
        @Order(15)
        @DisplayName("Should throw exception when blood collection ID is missing in OBR")
        void shouldThrowExceptionWhenBloodCollectionIdMissingInOBR() {
            // Given
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
        @Order(16)
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
        @Order(17)
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
                    .hasMessage("TestOrder not found for bloodCollectionId: TUBE-001");

            verify(testResultRepository, never()).save(any());
        }

        @Test
        @Order(18)
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
        @Order(19)
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
        @Order(20)
        @DisplayName("Should throw exception when segment separators are missing")
        void shouldThrowExceptionWhenSegmentSeparatorsMissing() {
            // Given
            String invalidHl7 = "MSH|^~\\&|BloodAnalyzer|Hospital Lab";

            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7(invalidHl7))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid HL7 format: Missing segment separators");
        }

        @Test
        @Order(21)
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
        @Order(22)
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
        @Order(23)
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

            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());
            assertThat(captor.getValue().getTestResultParameter()).isEmpty();
        }

        @Test
        @Order(24)
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
            assertThat(param.getParamName()).isEqualTo("WBC");
        }

        @Test
        @Order(25)
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
        @Order(26)
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
        @Order(27)
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
        @Order(28)
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

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResultParameter param = captor.getValue().getTestResultParameter().get(0);
            assertThat(param.getSequence()).isEqualTo(0);
        }

        @Test
        @Order(29)
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
        @Order(30)
        @DisplayName("Should ignore unknown segment types (e.g., PID)")
        void shouldIgnoreUnknownSegmentTypes() {
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
            assertThat(response.getStatusCode()).isEqualTo(200);
        }

        @Test
        @Order(31)
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
        @Order(32)
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

        @Test
        @Order(33)
        @DisplayName("Should set default flag to N when OBX-8 is missing")
        void shouldSetDefaultFlagWhenMissing() {
            // Given
            String hl7WithoutFlag = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab|LIS|Hospital|20241106103045||ORU^R01|MSG001|P|2.5
                    OBR|1|TUBE-001|CBC^Complete Blood Count
                    OBX|1|NM|WBC^White Blood Cell||7.5|10^3/uL|4.0-10.0|||F
                    """;

            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(hl7WithoutFlag);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResultParameter param = captor.getValue().getTestResultParameter().get(0);
            assertThat(param.getFlag()).isEqualTo("N");
        }

        @Test
        @Order(34)
        @DisplayName("Should set obxIdentifier to UNKNOWN when OBX-3 is missing")
        void shouldSetObxIdentifierToUnknownWhenMissing() {
            // Given
            String hl7WithoutIdentifier = """
                    MSH|^~\\&|BloodAnalyzer|Hospital Lab|LIS|Hospital|20241106103045||ORU^R01|MSG001|P|2.5
                    OBR|1|TUBE-001|CBC^Complete Blood Count
                    OBX|1|NM|||7.5|10^3/uL|4.0-10.0|N||F
                    """;

            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(hl7WithoutIdentifier);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(testResultRepository).save(captor.capture());

            TestResultParameter param = captor.getValue().getTestResultParameter().get(0);
            assertThat(param.getObxIdentifier()).isEqualTo("UNKNOWN");
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
        @DisplayName("Should republish event successfully when test result exists")
        void shouldRepublishEventSuccessfully() {
            // Given
            String testOrderId = "TO-001";
            testOrder.setTestResults(testResult);

            when(testOrderRepository.findById(testOrderId))
                    .thenReturn(Optional.of(testOrder));

            // When
            RestResponse<Void> response = testResultService.republishTestResultEvent(testOrderId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage())
                    .isEqualTo("Successfully triggered event republishing for test order " + testOrderId);
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getResult()).isNull();

            verify(testOrderRepository).findById(testOrderId);
            verify(eventPublisher).publishTestResultCreated(testResult);
        }

        @Test
        @Order(2)
        @DisplayName("Should throw exception when test order not found")
        void shouldThrowExceptionWhenOrderNotFound() {
            // Given
            String testOrderId = "INVALID-ID";
            when(testOrderRepository.findById(testOrderId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.republishTestResultEvent(testOrderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("TestOrder not found with id: " + testOrderId);

            verify(testOrderRepository).findById(testOrderId);
            verify(eventPublisher, never()).publishTestResultCreated(any());
        }

        @Test
        @Order(3)
        @DisplayName("Should throw exception when test result does not exist for order")
        void shouldThrowExceptionWhenNoTestResultExists() {
            // Given
            String testOrderId = "TO-001";
            testOrder.setTestResults(null);

            when(testOrderRepository.findById(testOrderId))
                    .thenReturn(Optional.of(testOrder));

            // When & Then
            assertThatThrownBy(() -> testResultService.republishTestResultEvent(testOrderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No test result found for this order to republish.");

            verify(testOrderRepository).findById(testOrderId);
            verify(eventPublisher, never()).publishTestResultCreated(any());
        }

        @Test
        @Order(4)
        @DisplayName("Should handle null test order ID")
        void shouldHandleNullTestOrderId() {
            // Given
            when(testOrderRepository.findById(null))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.republishTestResultEvent(null))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(testOrderRepository).findById(null);
        }

        @Test
        @Order(5)
        @DisplayName("Should verify event publisher is called with correct test result")
        void shouldVerifyEventPublisherCalledWithCorrectTestResult() {
            // Given
            String testOrderId = "TO-001";
            testOrder.setTestResults(testResult);

            when(testOrderRepository.findById(testOrderId))
                    .thenReturn(Optional.of(testOrder));

            // When
            testResultService.republishTestResultEvent(testOrderId);

            // Then
            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
            verify(eventPublisher).publishTestResultCreated(captor.capture());

            TestResult publishedResult = captor.getValue();
            assertThat(publishedResult).isEqualTo(testResult);
            assertThat(publishedResult.getResultId()).isEqualTo("TR-001");
            assertThat(publishedResult.getBloodCollectionId()).isEqualTo("TUBE-001");
        }

        @Test
        @Order(6)
        @DisplayName("Should handle repository exception gracefully")
        void shouldHandleRepositoryException() {
            // Given
            String testOrderId = "TO-001";
            when(testOrderRepository.findById(testOrderId))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> testResultService.republishTestResultEvent(testOrderId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(eventPublisher, never()).publishTestResultCreated(any());
        }

        @Test
        @Order(7)
        @DisplayName("Should handle event publisher exception")
        void shouldHandleEventPublisherException() {
            // Given
            String testOrderId = "TO-001";
            testOrder.setTestResults(testResult);

            when(testOrderRepository.findById(testOrderId))
                    .thenReturn(Optional.of(testOrder));
            doThrow(new RuntimeException("Kafka connection error"))
                    .when(eventPublisher).publishTestResultCreated(any());

            // When & Then
            assertThatThrownBy(() -> testResultService.republishTestResultEvent(testOrderId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Kafka connection error");

            verify(eventPublisher).publishTestResultCreated(testResult);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // REPROCESS HL7 BY BLOOD COLLECTION ID TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Reprocess HL7 By Blood Collection ID Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReprocessHl7Tests {

        @Test
        @Order(1)
        @DisplayName("Should reprocess HL7 successfully")
        void shouldReprocessHl7Successfully() {
            // Given
            String bloodCollectionId = "TUBE-001";
            testResult.setHl7RawData(validHl7Message);
            testOrder.setTestResults(testResult);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            RestResponse<TestResultResponse> response = testResultService.reprocessHl7ByBloodCollectionId(bloodCollectionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("HL7 data processed successfully");

            verify(testResultRepository).delete(testResult);
            verify(testResultRepository, times(2)).save(any(TestResult.class));
            verify(eventPublisher).publishTestResultCreated(any(TestResult.class));
        }

        @Test
        @Order(2)
        @DisplayName("Should throw exception when no existing test result found for reprocess")
        void shouldThrowExceptionWhenNoExistingResultFound() {
            // Given
            String bloodCollectionId = "INVALID-ID";
            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.reprocessHl7ByBloodCollectionId(bloodCollectionId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Cannot re-process. No existing TestResult found for: " + bloodCollectionId);

            verify(testResultRepository, never()).delete(any());
            verify(testResultRepository, never()).save(any());
        }

        @Test
        @Order(3)
        @DisplayName("Should delete old test result before reprocessing")
        void shouldDeleteOldTestResultBeforeReprocessing() {
            // Given
            String bloodCollectionId = "TUBE-001";
            testResult.setHl7RawData(validHl7Message);
            testOrder.setTestResults(testResult);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.reprocessHl7ByBloodCollectionId(bloodCollectionId);

            // Then
            verify(testResultRepository).delete(testResult);
        }

        @Test
        @Order(3)
        @DisplayName("Should reset test order status to PENDING")
        void shouldResetTestOrderStatusToPending() {
            // Given
            String bloodCollectionId = "TUBE-001";
            testResult.setHl7RawData(validHl7Message);
            testResult.setTestOrder(testOrder);
            testOrder.setStatus(TestOrderStatus.COMPLETED);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult));

            // When
            testResultService.deleteAndPrepareForReprocess(bloodCollectionId);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    order.getStatus() == TestOrderStatus.PENDING
            ));
        }

        @Test
        @Order(4)
        @DisplayName("Should clear test results from test order")
        void shouldClearTestResultsFromTestOrder() {
            // Given
            String bloodCollectionId = "TUBE-001";
            testResult.setHl7RawData(validHl7Message);
            testResult.setTestOrder(testOrder);
            testOrder.setTestResults(testResult);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult));

            // When
            testResultService.deleteAndPrepareForReprocess(bloodCollectionId);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    order.getTestResults() == null
            ));
        }

        @Test
        @Order(5)
        @DisplayName("Should handle deletion when test order is null")
        void shouldHandleDeletionWhenTestOrderIsNull() {
            // Given
            String bloodCollectionId = "TUBE-001";
            testResult.setHl7RawData(validHl7Message);
            testResult.setTestOrder(null);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult));

            // When
            String returnedHl7 = testResultService.deleteAndPrepareForReprocess(bloodCollectionId);

            // Then
            assertThat(returnedHl7).isEqualTo(validHl7Message);
            verify(testResultRepository).delete(testResult);
            verify(testOrderRepository, never()).save(any());
        }

        @Test
        @Order(6)
        @DisplayName("Should return HL7 data even if null")
        void shouldReturnHl7DataEvenIfNull() {
            // Given
            String bloodCollectionId = "TUBE-001";
            testResult.setHl7RawData(null);
            testResult.setTestOrder(testOrder);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult));

            // When
            String returnedHl7 = testResultService.deleteAndPrepareForReprocess(bloodCollectionId);

            // Then
            assertThat(returnedHl7).isNull();
            verify(testResultRepository).delete(testResult);
        }

        @Test
        @Order(7)
        @DisplayName("Should delete test result before updating test order")
        void shouldDeleteTestResultBeforeUpdatingTestOrder() {
            // Given
            String bloodCollectionId = "TUBE-001";
            testResult.setHl7RawData(validHl7Message);
            testResult.setTestOrder(testOrder);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult));

            // When
            testResultService.deleteAndPrepareForReprocess(bloodCollectionId);

            // Then
            var inOrder = inOrder(testResultRepository, testOrderRepository);
            inOrder.verify(testResultRepository).delete(testResult);
            inOrder.verify(testOrderRepository).save(any(TestOrder.class));
        }

        @Test
        @Order(8)
        @DisplayName("Should handle repository exception during deletion")
        void shouldHandleRepositoryExceptionDuringDeletion() {
            // Given
            String bloodCollectionId = "TUBE-001";
            testResult.setHl7RawData(validHl7Message);
            testResult.setTestOrder(testOrder);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult));
            doThrow(new RuntimeException("Database error"))
                    .when(testResultRepository).delete(any());

            // When & Then
            assertThatThrownBy(() -> testResultService.deleteAndPrepareForReprocess(bloodCollectionId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");
        }

        @Test
        @Order(9)
        @DisplayName("Should handle null blood collection ID")
        void shouldHandleNullBloodCollectionId() {
            // Given
            when(testResultRepository.findByBloodCollectionId(null))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.deleteAndPrepareForReprocess(null))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(testResultRepository, never()).delete(any());
        }

        @Test
        @Order(10)
        @DisplayName("Should handle empty blood collection ID")
        void shouldHandleEmptyBloodCollectionId() {
            // Given
            String emptyId = "";
            when(testResultRepository.findByBloodCollectionId(emptyId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.deleteAndPrepareForReprocess(emptyId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(testResultRepository, never()).delete(any());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // INTEGRATION TESTS - FULL WORKFLOW
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Tests - Full Workflow")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class IntegrationTests {

        @Test
        @Order(1)
        @DisplayName("Should process complete workflow: receive HL7, get result, republish event")
        void shouldProcessCompleteWorkflow() {
            // Given
            String bloodCollectionId = "TUBE-001";
            testOrder.setTestResults(testResult);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.empty())
                    .thenReturn(Optional.of(testResult));
            when(testOrderRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testOrder));
            when(testOrderRepository.findById("TO-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When - Step 1: Receive HL7
            RestResponse<TestResultResponse> receiveResponse = testResultService.receiveHl7(validHl7Message);

            // Then - Verify HL7 processing
            assertThat(receiveResponse.getStatusCode()).isEqualTo(200);
            verify(eventPublisher).publishTestResultCreated(any(TestResult.class));

            // When - Step 2: Get result by blood collection ID
            RestResponse<?> getResponse = testResultService.getResultByBloodCollectionId(bloodCollectionId);

            // Then - Verify retrieval
            assertThat(getResponse.getStatusCode()).isEqualTo(200);

            // When - Step 3: Republish event
            RestResponse<Void> republishResponse = testResultService.republishTestResultEvent("TO-001");

            // Then - Verify republishing
            assertThat(republishResponse.getStatusCode()).isEqualTo(200);
            verify(eventPublisher, times(2)).publishTestResultCreated(any(TestResult.class));
        }

        @Test
        @Order(2)
        @DisplayName("Should handle reprocess workflow completely")
        void shouldHandleReprocessWorkflowCompletely() {
            // Given
            String bloodCollectionId = "TUBE-001";
            testResult.setHl7RawData(validHl7Message);
            testOrder.setTestResults(testResult);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            RestResponse<TestResultResponse> response = testResultService.reprocessHl7ByBloodCollectionId(bloodCollectionId);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(200);
            verify(testResultRepository).delete(any(TestResult.class));
            verify(testResultRepository, times(2)).save(any(TestResult.class));
            verify(testOrderRepository, atLeast(2)).save(any(TestOrder.class));
            verify(eventPublisher).publishTestResultCreated(any(TestResult.class));
        }

        @Test
        @Order(3)
        @DisplayName("Should maintain data consistency across operations")
        void shouldMaintainDataConsistencyAcrossOperations() {
            // Given
            String bloodCollectionId = "TUBE-001";

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            testResultService.receiveHl7(validHl7Message);

            // Then - Verify all relationships are maintained
            ArgumentCaptor<TestResult> resultCaptor = ArgumentCaptor.forClass(TestResult.class);
            ArgumentCaptor<TestOrder> orderCaptor = ArgumentCaptor.forClass(TestOrder.class);

            verify(testResultRepository).save(resultCaptor.capture());
            verify(testOrderRepository).save(orderCaptor.capture());

            TestResult savedResult = resultCaptor.getValue();
            TestOrder savedOrder = orderCaptor.getValue();

            assertThat(savedResult.getTestOrder()).isNotNull();
            assertThat(savedResult.getBloodCollectionId()).isEqualTo(bloodCollectionId);
            assertThat(savedOrder.getStatus()).isEqualTo(TestOrderStatus.COMPLETED);

            savedResult.getTestResultParameter().forEach(param -> {
                assertThat(param.getTestResult()).isEqualTo(savedResult);
                assertThat(param.getTestOrder()).isEqualTo(savedOrder);
            });
        }

        @Test
        @Order(4)
        @DisplayName("Should handle concurrent-like operations with same blood collection ID")
        void shouldHandleConcurrentLikeOperations() {
            // Given
            String bloodCollectionId = "TUBE-001";

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When - First processing
            RestResponse<TestResultResponse> response1 = testResultService.receiveHl7(validHl7Message);

            // Then
            assertThat(response1.getStatusCode()).isEqualTo(200);

            // Given - Attempt duplicate processing
            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult));

            // When & Then - Should prevent duplicate
            assertThatThrownBy(() -> testResultService.receiveHl7(validHl7Message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TestResult already exists for bloodCollectionId: TUBE-001");
        }

        @Test
        @Order(5)
        @DisplayName("Should handle error recovery in workflow")
        void shouldHandleErrorRecoveryInWorkflow() {
            // Given
            String bloodCollectionId = "TUBE-001";

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class)))
                    .thenThrow(new RuntimeException("Database error"))
                    .thenReturn(testResult);

            // When & Then - First attempt fails
            assertThatThrownBy(() -> testResultService.receiveHl7(validHl7Message))
                    .isInstanceOf(RuntimeException.class);

            // When - Retry succeeds
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            RestResponse<TestResultResponse> response = testResultService.receiveHl7(validHl7Message);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(200);
        }
    }
}
                    .thenReturn(Optional.of(testOrder));
when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

// When
RestResponse<TestResultResponse> response = testResultService.reprocessHl7ByBloodCollectionId(bloodCollectionId);

// Then
assertThat(response).isNotNull();
assertThat(response.getStatusCode()).isEqualTo(200);
assertThat(response.getMessage()).isEqualTo("HL7 data processed successfully");

verify(testResultRepository).delete(testResult);
verify(testResultRepository, times(2)).save(any(TestResult.class));
verify(eventPublisher).publishTestResultCreated(any(TestResult.class));
        }

@Test
@Order(2)
@DisplayName("Should throw exception when no existing test result found for reprocess")
void shouldThrowExceptionWhenNoExistingResultFound() {
    // Given
    String bloodCollectionId = "INVALID-ID";
    when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
            .thenReturn(Optional.empty());

    // When & Then
    assertThatThrownBy(() -> testResultService.reprocessHl7ByBloodCollectionId(bloodCollectionId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Cannot re-process. No existing TestResult found for: " + bloodCollectionId);

    verify(testResultRepository, never()).delete(any());
    verify(testResultRepository, never()).save(any());
}

@Test
@Order(3)
@DisplayName("Should delete old test result before reprocessing")
void shouldDeleteOldTestResultBeforeReprocessing() {
    // Given
    String bloodCollectionId = "TUBE-001";
    testResult.setHl7RawData(validHl7Message);
    testOrder.setTestResults(testResult);

    when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
            .thenReturn(Optional.of(testResult))
            .thenReturn(Optional.empty());
    when(testOrderRepository.findByBloodCollectionId(bloodCollectionId))
            .thenReturn(Optional.of(testOrder));
    when(testResultRepository.save(any(TestResult.class))).thenReturn(testResult);
    when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
    when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

    // When
    testResultService.reprocessHl7ByBloodCollectionId(bloodCollectionId);

    // Then
    verify(testResultRepository).delete(testResult);
}

@Test
@Order(4)
@DisplayName("Should reset test order status to PENDING during reprocess")
void shouldResetTestOrderStatusToPending() {
    // Given
    String bloodCollectionId = "TUBE-001";
    testResult.setHl7RawData(validHl7Message);
    testOrder.setTestResults(testResult);
    testOrder.setStatus(TestOrderStatus.COMPLETED);

    when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
            .thenReturn(Optional.of(testResult))
            .thenReturn(Optional.empty());
    when(testOrderRepository.findByBloodCollectionId(bloodCollectionId))
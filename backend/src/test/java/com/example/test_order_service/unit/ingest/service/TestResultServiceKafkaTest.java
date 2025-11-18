package com.example.test_order_service.unit.ingest.service;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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

        // Setup test order
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
        @Order(5)
        @DisplayName("Should throw exception when HL7 message is blank")
        void shouldThrowExceptionWhenHl7IsBlank() {
            // When & Then
            assertThatThrownBy(() -> testResultService.receiveHl7("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("HL7 message is empty");

            verify(testResultRepository, never()).save(any());
        }

        @Test
        @Order(6)
        @DisplayName("Should throw exception when test result already exists")
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
    }

    // ═══════════════════════════════════════════════════════════════
    // REPROCESS HL7 TESTS
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

            // Mock để trả về testResult khi tìm kiếm lần đầu (để delete)
            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult))
                    .thenReturn(Optional.empty()); // Lần 2 trả về empty sau khi đã delete

            when(testOrderRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testOrder));

            // Tạo một TestResult mới cho lần save thứ 2
            TestResult newTestResult = new TestResult();
            newTestResult.setBloodCollectionId(bloodCollectionId);

            // Mock save() để trả về các đối tượng khác nhau
            when(testResultRepository.save(any(TestResult.class)))
                    .thenReturn(testResult)      // Lần save đầu tiên
                    .thenReturn(newTestResult);  // Lần save thứ hai (sau re-ingest)

            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class))).thenReturn(testResultResponse);

            // When
            RestResponse<TestResultResponse> response = testResultService.reprocessHl7ByBloodCollectionId(bloodCollectionId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("HL7 data processed successfully");

            verify(testResultRepository).delete(testResult);
            verify(testResultRepository, times(1)).save(any(TestResult.class));
            verify(testResultRepository, atLeastOnce()).save(any(TestResult.class));
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
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE AND PREPARE FOR REPROCESS TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Delete And Prepare For Reprocess Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteAndPrepareForReprocessTests {

        @Test
        @Order(1)
        @DisplayName("Should delete test result and return HL7 data")
        void shouldDeleteTestResultAndReturnHl7Data() {
            // Given
            String bloodCollectionId = "TUBE-001";
            String expectedHl7 = validHl7Message;
            testResult.setHl7RawData(expectedHl7);
            testResult.setTestOrder(testOrder);

            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.of(testResult));

            // When
            String returnedHl7 = testResultService.deleteAndPrepareForReprocess(bloodCollectionId);

            // Then
            assertThat(returnedHl7).isEqualTo(expectedHl7);
            verify(testResultRepository).delete(testResult);
        }

        @Test
        @Order(2)
        @DisplayName("Should throw exception when test result not found for deletion")
        void shouldThrowExceptionWhenTestResultNotFoundForDeletion() {
            // Given
            String bloodCollectionId = "INVALID-ID";
            when(testResultRepository.findByBloodCollectionId(bloodCollectionId))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testResultService.deleteAndPrepareForReprocess(bloodCollectionId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Cannot re-process. No existing TestResult found for: " + bloodCollectionId);

            verify(testResultRepository, never()).delete(any());
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
    }
}
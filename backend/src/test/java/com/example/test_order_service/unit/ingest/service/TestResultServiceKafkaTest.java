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
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TestResultServiceKafka Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestResultServiceKafkaTest {

    @Mock private TestResultRepository testResultRepository;
    @Mock private TestOrderRepository testOrderRepository;
    @Mock private TestResultMapper testResultMapper;
    @Mock private TestResultEventPublisher eventPublisher;

    @InjectMocks private TestResultServiceKafka testResultService;

    private TestOrder testOrder;
    private TestResult testResult;
    private TestResultResponse testResultResponse;
    private String validHl7Message;

    @BeforeEach
    void setUp() {
        testResultService.setSelf(testResultService);

        testOrder = new TestOrder();
        testOrder.setTestOrderId("TO-001");
        testOrder.setBloodCollectionId("TUBE-001");
        testOrder.setPatientId(1);
        testOrder.setPatientName("Nguyen Van An");
        testOrder.setDateOfBirth(LocalDate.of(1985, 3, 15));
        testOrder.setStatus(TestOrderStatus.PENDING);

        testResult = new TestResult();
        testResult.setResultId("TR-001");
        testResult.setTestOrder(testOrder);
        testResult.setBloodCollectionId("TUBE-001");
        testResult.setInstrumentName("BloodAnalyzer");
        testResult.setStatus("COMPLETED");
        testResult.setHl7RawData("MSH|^~\\&|BloodAnalyzer|Lab|...");
        testResult.setTestResultParameter(new ArrayList<>());

        testResultResponse = TestResultResponse.builder()
                .bloodCollectionId("TUBE-001")
                .instrumentName("BloodAnalyzer")
                .status("COMPLETED")
                .hl7RawData("MSH|^~\\&|BloodAnalyzer|Lab|...")
                .testResultParameter(new ArrayList<>())
                .build();

        // NOTE: PID-3 must be numeric in your service
        validHl7Message = """
                MSH|^~\\&|BloodAnalyzer|Lab|LIS|Hospital|20251103093000||ORU^R01|MSG20251103093000001|P|2.5.1
                PID|1||1||Nguyen^Van An||19850315|Male
                OBR|1|TUBE-001|CBC^Complete Blood Count|||20251103093000
                OBX|1|NM|WBC^White Blood Cells||7.2|10^3/uL|4.0-10.0|N|||F
                """;
    }

    // =========================================================
    // GET RESULT BY BLOOD COLLECTION ID
    // =========================================================
    @Nested
    @DisplayName("Get Result By Blood Collection ID Tests")
    class GetResultByBloodCollectionIdTests {

        @Test
        @DisplayName("Should get test result successfully by blood collection ID")
        void shouldGetTestResultSuccessfully() {
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testResult));
            when(testResultMapper.toTestResultResponse(testResult))
                    .thenReturn(testResultResponse);

            RestResponse<?> response =
                    testResultService.getResultByBloodCollectionId("TUBE-001");

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Retrieved test result successfully");
            assertThat(response.getResult()).isNotNull();

            verify(testResultRepository).findByBloodCollectionId("TUBE-001");
            verify(testResultMapper).toTestResultResponse(testResult);
            verifyNoInteractions(eventPublisher);
        }

        @Test
        @DisplayName("Should throw exception when blood collection ID not found")
        void shouldThrowExceptionWhenBloodCollectionIdNotFound() {
            when(testResultRepository.findByBloodCollectionId("INVALID"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> testResultService.getResultByBloodCollectionId("INVALID"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Test result not found for blood collection Id: INVALID");

            verify(testResultRepository).findByBloodCollectionId("INVALID");
            verify(testResultMapper, never()).toTestResultResponse(any());
        }
    }

    // =========================================================
    // RECEIVE HL7
    // =========================================================
    @Nested
    @DisplayName("Receive HL7 Message Tests")
    class ReceiveHl7Tests {

        @Test
        @DisplayName("Should process valid HL7 message successfully")
        void shouldProcessValidHl7MessageSuccessfully() {
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class)))
                    .thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class)))
                    .thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class)))
                    .thenReturn(testResultResponse);

            RestResponse<TestResultResponse> response =
                    testResultService.receiveHl7(validHl7Message);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("HL7 data processed successfully");
            assertThat(response.getResult()).isNotNull();

            verify(testResultRepository).findByBloodCollectionId("TUBE-001");
            verify(testOrderRepository).findByBloodCollectionId("TUBE-001");
            verify(testResultRepository).save(any(TestResult.class));
            verify(testOrderRepository).save(argThat(o -> o.getStatus() == TestOrderStatus.COMPLETED));
            verify(eventPublisher).publishTestResultCreated(any(TestResult.class));
        }

        @Test
        @DisplayName("Should throw exception when HL7 message is null")
        void shouldThrowExceptionWhenHl7IsNull() {
            assertThatThrownBy(() -> testResultService.receiveHl7(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("HL7 message is empty");

            verifyNoInteractions(testResultRepository, testOrderRepository, testResultMapper, eventPublisher);
        }

        @Test
        @DisplayName("Should throw exception when HL7 message is blank")
        void shouldThrowExceptionWhenHl7IsBlank() {
            assertThatThrownBy(() -> testResultService.receiveHl7("   "))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("HL7 message is empty");

            verifyNoInteractions(testResultRepository, testOrderRepository, testResultMapper, eventPublisher);
        }

        @Test
        @DisplayName("Should throw exception when MSH/PID/OBR missing")
        void shouldThrowWhenMissingRequiredSegments() {
            String hl7MissingObr = """
                    MSH|^~\\&|Inst|Lab|LIS|Hospital|20251103093000||ORU^R01|MSG|P|2.5.1
                    PID|1||1||A||19850315|Male
                    """;

            assertThatThrownBy(() -> testResultService.receiveHl7(hl7MissingObr))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Invalid HL7 format");

            verifyNoInteractions(testResultRepository, testOrderRepository, testResultMapper, eventPublisher);
        }

        @Test
        @DisplayName("Should throw exception when PID patientId not numeric")
        void shouldThrowWhenPatientIdNotNumeric() {
            String hl7BadPid = """
                    MSH|^~\\&|Inst|Lab|LIS|Hospital|20251103093000||ORU^R01|MSG|P|2.5.1
                    PID|1||PAT-2025-001||A||19850315|Male
                    OBR|1|TUBE-001|CBC^Complete Blood Count|||20251103093000
                    """;

            assertThatThrownBy(() -> testResultService.receiveHl7(hl7BadPid))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("HL7's Patient ID is not valid: PAT-2025-001");

            verifyNoInteractions(testResultRepository, testOrderRepository, testResultMapper, eventPublisher);
        }

        @Test
        @DisplayName("Should throw exception when blood collection ID missing in OBR-2")
        void shouldThrowWhenBloodCollectionIdMissing() {
            String hl7MissingTube = """
                    MSH|^~\\&|Inst|Lab|LIS|Hospital|20251103093000||ORU^R01|MSG|P|2.5.1
                    PID|1||1||A||19850315|Male
                    OBR|1|||CBC^Complete Blood Count|||20251103093000
                    """;

            assertThatThrownBy(() -> testResultService.receiveHl7(hl7MissingTube))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Missing blood collection ID in OBR-2 (Placer Order Number). BloodCollectionId is required and cannot be empty.");

            verifyNoInteractions(testResultRepository, testOrderRepository, testResultMapper, eventPublisher);
        }

        @Test
        @DisplayName("Should throw exception when test result already exists")
        void shouldThrowExceptionWhenTestResultAlreadyExists() {
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testResult));

            assertThatThrownBy(() -> testResultService.receiveHl7(validHl7Message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("TestResult already exists for bloodCollectionId: TUBE-001");

            verify(testResultRepository).findByBloodCollectionId("TUBE-001");
            verify(testOrderRepository, never()).findByBloodCollectionId(anyString());
            verify(testResultRepository, never()).save(any());
            verify(eventPublisher, never()).publishTestResultCreated(any());
        }

        @Test
        @DisplayName("Should throw exception when order not found for tube")
        void shouldThrowWhenOrderNotFound() {
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> testResultService.receiveHl7(validHl7Message))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("TestOrder not found for bloodCollectionId");

            verify(testOrderRepository).findByBloodCollectionId("TUBE-001");
            verify(testResultRepository, never()).save(any());
            verify(eventPublisher, never()).publishTestResultCreated(any());
        }

        @Test
        @DisplayName("Should not publish event when save fails")
        void shouldNotPublishEventWhenSaveFails() {
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class)))
                    .thenThrow(new RuntimeException("db down"));

            assertThatThrownBy(() -> testResultService.receiveHl7(validHl7Message))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("db down");

            verify(eventPublisher, never()).publishTestResultCreated(any());
        }

        @Test
        @DisplayName("Should handle mapper returns null response")
        void shouldHandleMapperNullResponseBranch() {
            when(testResultRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.empty());
            when(testOrderRepository.findByBloodCollectionId("TUBE-001"))
                    .thenReturn(Optional.of(testOrder));
            when(testResultRepository.save(any(TestResult.class)))
                    .thenReturn(testResult);
            when(testOrderRepository.save(any(TestOrder.class)))
                    .thenReturn(testOrder);
            when(testResultMapper.toTestResultResponse(any(TestResult.class)))
                    .thenReturn(null);

            RestResponse<TestResultResponse> response =
                    testResultService.receiveHl7(validHl7Message);

            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getResult()).isNull();
            verify(eventPublisher).publishTestResultCreated(any(TestResult.class));
        }
    }

    // =========================================================
    // REPUBLISH / REPROCESS / DELETE
    // =========================================================
    @Nested
    @DisplayName("Republish / Reprocess / Delete Tests")
    class RepublishAndReprocessTests {

        @Test
        @DisplayName("republish should republish when testResult exists")
        void republish_success() {
            testOrder.setTestResults(testResult);
            when(testOrderRepository.findById("TO-001"))
                    .thenReturn(Optional.of(testOrder));

            RestResponse<Void> res = testResultService.republishTestResultEvent("TO-001");

            assertThat(res.getStatusCode()).isEqualTo(200);
            verify(eventPublisher).publishTestResultCreated(testResult);
        }

        @Test
        @DisplayName("republish should throw when order not found")
        void republish_notFound() {
            when(testOrderRepository.findById("X"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> testResultService.republishTestResultEvent("X"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("TestOrder not found with id: X");
        }

        @Test
        @DisplayName("republish should throw when no testResult")
        void republish_noResult() {
            testOrder.setTestResults(null);
            when(testOrderRepository.findById("TO-001"))
                    .thenReturn(Optional.of(testOrder));

            assertThatThrownBy(() -> testResultService.republishTestResultEvent("TO-001"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("No test result found for this order to republish.");
        }

        @Test
        @DisplayName("deleteAndPrepare should throw when no existing result")
        void deleteAndPrepare_notFound() {
            when(testResultRepository.findByBloodCollectionId("X"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> testResultService.deleteAndPrepareForReprocess("X"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Cannot re-process. No existing TestResult found for: X");
        }

        @Test
        @DisplayName("reprocess should throw when no existing result")
        void reprocess_notFound() {
            when(testResultRepository.findByBloodCollectionId("X"))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> testResultService.reprocessHl7ByBloodCollectionId("X"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Cannot re-process. No existing TestResult found for: X");
        }
    }
}

//package com.example.test_order_service.unit.ingest.service;
//
//import com.example.test_order_service.entity.TestOrder;
//import com.example.test_order_service.entity.TestResult;
//import com.example.test_order_service.entity.TestResultParameter;
//import com.example.test_order_service.entity.enumForEntity.Gender;
//import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
//import com.example.test_order_service.exception.ResourceNotFoundException;
//import com.example.test_order_service.ingest.dto.InstrumentResultPayload;
//import com.example.test_order_service.ingest.service.TestResultIngestFullService;
//import com.example.test_order_service.repository.TestOrderRepository;
//import com.example.test_order_service.repository.TestResultRepository;
//import org.junit.jupiter.api.*;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.OffsetDateTime;
//import java.time.ZoneOffset;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Comprehensive Unit Tests for TestResultIngestFullService
// * Tests Kafka ingestion logic with full coverage
// */
//@ExtendWith(MockitoExtension.class)
//@DisplayName("TestResultIngestFullService Unit Tests")
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//public class TestResultIngestFullServiceTest {
//
//    @Mock
//    private TestResultRepository testResultRepository;
//
//    @Mock
//    private TestOrderRepository testOrderRepository;
//
//    @InjectMocks
//    private TestResultIngestFullService ingestService;
//
//    private TestOrder testOrder;
//    private InstrumentResultPayload payload;
//    private InstrumentResultPayload.Metadata metadata;
//    private List<InstrumentResultPayload.ResultItem> resultItems;
//
//    @BeforeEach
//    void setUp() {
//        // Setup test order
//        testOrder = TestOrder.builder()
//                .testOrderId("TO-001")
//                .patientId("PAT-2025-001")
//                .bloodCollectionId("TUBE-001")
//                .patientName("Nguyen Van An")
//                .dateOfBirth(LocalDate.of(1985, 3, 15))
//                .gender(Gender.MALE)
//                .status(TestOrderStatus.PENDING)
//                .build();
//
//        // Setup metadata
//        metadata = InstrumentResultPayload.Metadata.builder()
//                .instrumentId("BloodAnalyzer-01")
//                .technician("Dr. John Doe")
//                .build();
//
//        // Setup result items
//        InstrumentResultPayload.ResultItem wbc = InstrumentResultPayload.ResultItem.builder()
//                .parameter("WBC")
//                .value("7.2")
//                .unit("10^3/uL")
//                .referenceRange("4.0-10.0")
//                .flagged(false)
//                .build();
//
//        InstrumentResultPayload.ResultItem rbc = InstrumentResultPayload.ResultItem.builder()
//                .parameter("RBC")
//                .value("4.85")
//                .unit("10^6/uL")
//                .referenceRange("3.5-5.5")
//                .flagged(false)
//                .build();
//
//        InstrumentResultPayload.ResultItem hgb = InstrumentResultPayload.ResultItem.builder()
//                .parameter("HGB")
//                .value("145")
//                .unit("g/dL")
//                .referenceRange("110.0-160.0")
//                .flagged(false)
//                .build();
//
//        resultItems = Arrays.asList(wbc, rbc, hgb);
//
//        // Setup payload
//        payload = InstrumentResultPayload.builder()
//                .testOrderId("TO-001")
//                .patientId("PAT-2025-001")
//                .analyzedAt(OffsetDateTime.of(2025, 11, 6, 10, 30, 0, 0, ZoneOffset.UTC))
//                .results(resultItems)
//                .metadata(metadata)
//                .build();
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // SUCCESSFUL INGESTION SCENARIOS
//    // ═══════════════════════════════════════════════════════════════
//
//    @Nested
//    @DisplayName("Successful Ingestion Scenarios")
//    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    class SuccessfulIngestionTests {
//
//        @Test
//        @Order(1)
//        @DisplayName("Should ingest full payload successfully")
//        void shouldIngestFullPayloadSuccessfully() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            assertThat(savedResult).isNotNull();
//            assertThat(savedResult.getTestOrder()).isEqualTo(testOrder);
//            assertThat(savedResult.getPatientId()).isEqualTo("PAT-2025-001");
//            assertThat(savedResult.getBloodCollectionId()).isEqualTo("TUBE-001");
//            assertThat(savedResult.getInstrumentName()).isEqualTo("BloodAnalyzer-01");
//            assertThat(savedResult.getStatus()).isEqualTo("COMPLETED");
//        }
//
//        @Test
//        @Order(2)
//        @DisplayName("Should create test result with correct metadata")
//        void shouldCreateTestResultWithCorrectMetadata() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            assertThat(savedResult.getCreatedBy()).isEqualTo("Dr. John Doe");
//            assertThat(savedResult.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 11, 6, 10, 30, 0));
//            assertThat(savedResult.getHl7RawData()).isEqualTo("N/A - Ingested from Kafka as JSON");
//        }
//
//        @Test
//        @Order(3)
//        @DisplayName("Should create all test result parameters")
//        void shouldCreateAllTestResultParameters() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            List<TestResultParameter> parameters = savedResult.getTestResultParameter();
//
//            assertThat(parameters).hasSize(3);
//
//            // Verify WBC parameter
//            TestResultParameter wbc = parameters.get(0);
//            assertThat(wbc.getParamCode()).isEqualTo("WBC");
//            assertThat(wbc.getParamName()).isEqualTo("WBC");
//            assertThat(wbc.getValue()).isEqualTo("7.2");
//            assertThat(wbc.getUnit()).isEqualTo("10^3/uL");
//            assertThat(wbc.getRefRange()).isEqualTo("4.0-10.0");
//            assertThat(wbc.getFlag()).isEqualTo("Normal");
//            assertThat(wbc.getSequence()).isEqualTo(1);
//            assertThat(wbc.getComputedBy()).isEqualTo("instrument-service");
//        }
//
//        @Test
//        @Order(4)
//        @DisplayName("Should set flagged parameter to Abnormal")
//        void shouldSetFlaggedParameterToAbnormal() {
//            // Given
//            InstrumentResultPayload.ResultItem abnormalResult = InstrumentResultPayload.ResultItem.builder()
//                    .parameter("GLUCOSE")
//                    .value("150")
//                    .unit("mg/dL")
//                    .referenceRange("70-100")
//                    .flagged(true) // Abnormal
//                    .build();
//
//            payload.setResults(Collections.singletonList(abnormalResult));
//
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            TestResultParameter parameter = savedResult.getTestResultParameter().get(0);
//
//            assertThat(parameter.getFlag()).isEqualTo("Abnormal");
//        }
//
//        @Test
//        @Order(5)
//        @DisplayName("Should update test order status to COMPLETED")
//        void shouldUpdateTestOrderStatusToCompleted() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            assertThat(testOrder.getStatus()).isEqualTo(TestOrderStatus.COMPLETED);
//            assertThat(testOrder.getRunAt()).isNotNull();
//            assertThat(testOrder.getRunBy()).isEqualTo("Dr. John Doe");
//            assertThat(testOrder.getTestResults()).isNotNull();
//        }
//
//        @Test
//        @Order(6)
//        @DisplayName("Should establish bidirectional relationship between TestOrder and TestResult")
//        void shouldEstablishBidirectionalRelationship() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//
//            // Verify bidirectional relationship
//            assertThat(savedResult.getTestOrder()).isEqualTo(testOrder);
//            assertThat(testOrder.getTestResults()).isEqualTo(savedResult);
//        }
//
//        @Test
//        @Order(7)
//        @DisplayName("Should set sequence numbers correctly for multiple parameters")
//        void shouldSetSequenceNumbersCorrectly() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            List<TestResultParameter> parameters = savedResult.getTestResultParameter();
//
//            assertThat(parameters.get(0).getSequence()).isEqualTo(1);
//            assertThat(parameters.get(1).getSequence()).isEqualTo(2);
//            assertThat(parameters.get(2).getSequence()).isEqualTo(3);
//        }
//
//        @Test
//        @Order(8)
//        @DisplayName("Should link parameters to both test result and test order")
//        void shouldLinkParametersToBothTestResultAndTestOrder() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//
//            for (TestResultParameter parameter : savedResult.getTestResultParameter()) {
//                assertThat(parameter.getTestResult()).isEqualTo(savedResult);
//                assertThat(parameter.getTestOrder()).isEqualTo(testOrder);
//            }
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // ERROR HANDLING SCENARIOS
//    // ═══════════════════════════════════════════════════════════════
//
//    @Nested
//    @DisplayName("Error Handling Scenarios")
//    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    class ErrorHandlingTests {
//
//        @Test
//        @Order(1)
//        @DisplayName("Should throw ResourceNotFoundException when test order not found")
//        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.empty());
//
//            // When & Then
//            assertThatThrownBy(() -> ingestService.ingestFullPayload(payload))
//                    .isInstanceOf(ResourceNotFoundException.class)
//                    .hasMessage("Cannot ingest result. TestOrder not found for ID: TO-001");
//
//            verify(testResultRepository, never()).save(any());
//        }
//
//        @Test
//        @Order(2)
//        @DisplayName("Should throw IllegalArgumentException when patient ID mismatch")
//        void shouldThrowIllegalArgumentExceptionWhenPatientIdMismatch() {
//            // Given
//            testOrder.setPatientId("DIFFERENT-PATIENT-ID");
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//
//            // When & Then
//            assertThatThrownBy(() -> ingestService.ingestFullPayload(payload))
//                    .isInstanceOf(IllegalArgumentException.class)
//                    .hasMessage("Patient ID mismatch.");
//
//            verify(testResultRepository, never()).save(any());
//        }
//
//        @Test
//        @Order(3)
//        @DisplayName("Should skip ingestion when test result already exists")
//        void shouldSkipIngestionWhenTestResultAlreadyExists() {
//            // Given
//            TestResult existingResult = TestResult.builder().resultId("TR-001").build();
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.of(existingResult));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then - No save should be called
//            verify(testResultRepository, never()).save(any());
//        }
//
//        @Test
//        @Order(4)
//        @DisplayName("Should allow null patient ID in payload")
//        void shouldAllowNullPatientIdInPayload() {
//            // Given
//            payload.setPatientId(null);
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When - Should not throw exception
//            assertThatCode(() -> ingestService.ingestFullPayload(payload))
//                    .doesNotThrowAnyException();
//
//            // Then
//            verify(testResultRepository).save(any(TestResult.class));
//        }
//
//        @Test
//        @Order(5)
//        @DisplayName("Should allow matching patient IDs")
//        void shouldAllowMatchingPatientIds() {
//            // Given - Both have same patient ID
//            testOrder.setPatientId("PAT-2025-001");
//            payload.setPatientId("PAT-2025-001");
//
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When - Should not throw exception
//            assertThatCode(() -> ingestService.ingestFullPayload(payload))
//                    .doesNotThrowAnyException();
//
//            // Then
//            verify(testResultRepository).save(any(TestResult.class));
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // NULL AND EDGE CASE HANDLING
//    // ═══════════════════════════════════════════════════════════════
//
//    @Nested
//    @DisplayName("Null and Edge Case Handling")
//    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    class NullAndEdgeCaseTests {
//
//        @Test
//        @Order(1)
//        @DisplayName("Should handle null metadata with default values")
//        void shouldHandleNullMetadataWithDefaultValues() {
//            // Given
//            payload.setMetadata(null);
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            assertThat(savedResult.getInstrumentName()).isEqualTo("Unknown");
//            assertThat(savedResult.getCreatedBy()).isEqualTo("KafkaIngest");
//        }
//
//        @Test
//        @Order(2)
//        @DisplayName("Should handle null analyzedAt with current timestamp")
//        void shouldHandleNullAnalyzedAtWithCurrentTimestamp() {
//            // Given
//            payload.setAnalyzedAt(null);
//            LocalDateTime beforeIngestion = LocalDateTime.now();
//
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            assertThat(savedResult.getCreatedAt()).isAfterOrEqualTo(beforeIngestion);
//            assertThat(savedResult.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
//        }
//
//        @Test
//        @Order(3)
//        @DisplayName("Should handle empty results list")
//        void shouldHandleEmptyResultsList() {
//            // Given
//            payload.setResults(Collections.emptyList());
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            assertThat(savedResult.getTestResultParameter()).isEmpty();
//        }
//
//        @Test
//        @Order(4)
//        @DisplayName("Should handle single result item")
//        void shouldHandleSingleResultItem() {
//            // Given
//            InstrumentResultPayload.ResultItem singleItem = InstrumentResultPayload.ResultItem.builder()
//                    .parameter("GLUCOSE")
//                    .value("95")
//                    .unit("mg/dL")
//                    .referenceRange("70-100")
//                    .flagged(false)
//                    .build();
//
//            payload.setResults(Collections.singletonList(singleItem));
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            assertThat(savedResult.getTestResultParameter()).hasSize(1);
//            assertThat(savedResult.getTestResultParameter().get(0).getParamCode()).isEqualTo("GLUCOSE");
//        }
//
//        @Test
//        @Order(5)
//        @DisplayName("Should handle null instrument ID in metadata")
//        void shouldHandleNullInstrumentIdInMetadata() {
//            // Given
//            metadata.setInstrumentId(null);
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            // Implementation checks if metadata != null, not if instrumentId is null
//            // So when metadata exists but instrumentId is null, the result will be null
//            assertThat(savedResult.getInstrumentName()).isNull();
//        }
//
//        @Test
//        @Order(6)
//        @DisplayName("Should handle null technician in metadata")
//        void shouldHandleNullTechnicianInMetadata() {
//            // Given
//            metadata.setTechnician(null);
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            // Implementation checks if metadata != null, not if technician is null
//            // So when metadata exists but technician is null, the result will be null
//            assertThat(savedResult.getCreatedBy()).isNull();
//        }
//
//        @Test
//        @Order(7)
//        @DisplayName("Should handle null patient ID in test order")
//        void shouldHandleNullPatientIdInTestOrder() {
//            // Given
//            testOrder.setPatientId(null);
//            payload.setPatientId(null);
//
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            assertThat(savedResult.getPatientId()).isNull();
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // DATA INTEGRITY VALIDATION
//    // ═══════════════════════════════════════════════════════════════
//
//    @Nested
//    @DisplayName("Data Integrity Validation")
//    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    class DataIntegrityTests {
//
//        @Test
//        @Order(1)
//        @DisplayName("Should verify test result contains correct blood collection ID from order")
//        void shouldVerifyTestResultContainsCorrectBloodCollectionId() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            assertThat(savedResult.getBloodCollectionId()).isEqualTo(testOrder.getBloodCollectionId());
//        }
//
//        @Test
//        @Order(2)
//        @DisplayName("Should verify all parameters have consistent timestamps")
//        void shouldVerifyAllParametersHaveConsistentTimestamps() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            LocalDateTime resultTimestamp = savedResult.getCreatedAt();
//
//            for (TestResultParameter parameter : savedResult.getTestResultParameter()) {
//                assertThat(parameter.getCreatedAt()).isEqualTo(resultTimestamp);
//                assertThat(parameter.getCreatedBy()).isEqualTo(savedResult.getCreatedBy());
//            }
//        }
//
//        @Test
//        @Order(3)
//        @DisplayName("Should verify test order runAt matches result createdAt")
//        void shouldVerifyTestOrderRunAtMatchesResultCreatedAt() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            ArgumentCaptor<TestResult> captor = ArgumentCaptor.forClass(TestResult.class);
//            verify(testResultRepository).save(captor.capture());
//
//            TestResult savedResult = captor.getValue();
//            assertThat(testOrder.getRunAt()).isEqualTo(savedResult.getCreatedAt());
//            assertThat(testOrder.getRunBy()).isEqualTo(savedResult.getCreatedBy());
//        }
//
//        @Test
//        @Order(4)
//        @DisplayName("Should only call repository save once")
//        void shouldOnlyCallRepositorySaveOnce() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            verify(testResultRepository, times(1)).save(any(TestResult.class));
//        }
//    }
//
//    // ═══════════════════════════════════════════════════════════════
//    // TRANSACTION BEHAVIOR VERIFICATION
//    // ═══════════════════════════════════════════════════════════════
//
//    @Nested
//    @DisplayName("Transaction Behavior Verification")
//    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    class TransactionBehaviorTests {
//
//        @Test
//        @Order(1)
//        @DisplayName("Should call operations in correct sequence")
//        void shouldCallOperationsInCorrectSequence() {
//            // Given
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.empty());
//            when(testResultRepository.save(any(TestResult.class))).thenAnswer(invocation -> invocation.getArgument(0));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then - Verify order of operations
//            var inOrder = inOrder(testOrderRepository, testResultRepository);
//            inOrder.verify(testOrderRepository).findById("TO-001");
//            inOrder.verify(testResultRepository).findByTestOrder(testOrder);
//            inOrder.verify(testResultRepository).save(any(TestResult.class));
//        }
//
//        @Test
//        @Order(2)
//        @DisplayName("Should not save when duplicate check returns existing result")
//        void shouldNotSaveWhenDuplicateCheckReturnsExistingResult() {
//            // Given
//            TestResult existingResult = TestResult.builder().resultId("TR-EXISTING").build();
//            when(testOrderRepository.findById("TO-001")).thenReturn(Optional.of(testOrder));
//            when(testResultRepository.findByTestOrder(testOrder)).thenReturn(Optional.of(existingResult));
//
//            // When
//            ingestService.ingestFullPayload(payload);
//
//            // Then
//            verify(testOrderRepository).findById("TO-001");
//            verify(testResultRepository).findByTestOrder(testOrder);
//            verify(testResultRepository, never()).save(any());
//        }
//    }
//}

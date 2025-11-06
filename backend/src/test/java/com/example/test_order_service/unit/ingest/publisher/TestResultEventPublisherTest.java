package com.example.test_order_service.unit.ingest.publisher;

import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.TestResultParameter;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.ingest.dto.MonitoringEventPayload;
import com.example.test_order_service.ingest.dto.TestResultParameterEventPayload;
import com.example.test_order_service.ingest.publisher.TestResultEventPublisher;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for TestResultEventPublisher
 * Tests Kafka event publishing logic with full coverage
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestResultEventPublisher Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestResultEventPublisherTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private TestResultEventPublisher testResultEventPublisher;

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
                .paramCode("WBC")
                .paramName("White Blood Cells")
                .value("7.2")
                .unit("10^3/uL")
                .refRange("4.0-10.0")
                .flag("N")
                .build();

        parameter2 = TestResultParameter.builder()
                .paramCode("RBC")
                .paramName("Red Blood Cells")
                .value("4.85")
                .unit("10^6/uL")
                .refRange("3.5-5.5")
                .flag("N")
                .build();

        // Setup test result
        testResult = TestResult.builder()
                .resultId("TR-001")
                .testOrder(testOrder)
                .patientId("PAT-2025-001")
                .bloodCollectionId("TUBE-001")
                .instrumentName("BloodAnalyzer")
                .status("COMPLETED")
                .hl7RawData("MSH|^~\\&|BloodAnalyzer|...")
                .testResultParameter(new ArrayList<>(Arrays.asList(parameter1, parameter2)))
                .build();
        testResult.setCreatedBy("System");
        testResult.setCreatedAt(LocalDateTime.of(2025, 11, 6, 10, 30, 0));
    }

    // ═══════════════════════════════════════════════════════════════
    // PUBLISH TEST RESULT CREATED - SUCCESS SCENARIOS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Publish Test Result Created - Success Scenarios")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class PublishTestResultCreatedSuccessTests {

        @Test
        @Order(1)
        @DisplayName("Should publish test result event successfully")
        void shouldPublishTestResultEventSuccessfully() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);

            verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), eventCaptor.capture());

            // Verify topic
            assertThat(topicCaptor.getValue()).isEqualTo("test-results-events");

            // Verify key (correlation ID = blood collection ID)
            assertThat(keyCaptor.getValue()).isEqualTo("TUBE-001");

            // Verify event payload
            MonitoringEventPayload event = eventCaptor.getValue();
            assertThat(event).isNotNull();
            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getLogType()).isEqualTo("EVENT");
            assertThat(event.getAction()).isEqualTo("TEST_RESULT_CREATED");
            assertThat(event.getServiceName()).isEqualTo("test-order-service");
            assertThat(event.getCorrelationId()).isEqualTo("TUBE-001");
            assertThat(event.getSeverity()).isEqualTo("INFO");
            assertThat(event.getOperatorId()).isEqualTo("System");
        }

        @Test
        @Order(2)
        @DisplayName("Should include correct payload data in event")
        void shouldIncludeCorrectPayloadDataInEvent() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            Map<String, Object> payload = event.getPayload();

            assertThat(payload).isNotNull();
            assertThat(payload.get("testOrderId")).isEqualTo("TO-001");
            assertThat(payload.get("patientId")).isEqualTo("PAT-2025-001");
            assertThat(payload.get("bloodCollectionId")).isEqualTo("TUBE-001");
            assertThat(payload.get("instrumentName")).isEqualTo("BloodAnalyzer");
            assertThat(payload.get("status")).isEqualTo("COMPLETED");
        }

        @Test
        @Order(3)
        @DisplayName("Should include test result parameters in payload")
        void shouldIncludeTestResultParametersInPayload() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            Map<String, Object> payload = event.getPayload();

            @SuppressWarnings("unchecked")
            List<TestResultParameterEventPayload> parameters =
                    (List<TestResultParameterEventPayload>) payload.get("parameters");

            assertThat(parameters).isNotNull();
            assertThat(parameters).hasSize(2);

            // Verify first parameter
            TestResultParameterEventPayload param1 = parameters.get(0);
            assertThat(param1.getParamCode()).isEqualTo("WBC");
            assertThat(param1.getParamName()).isEqualTo("White Blood Cells");
            assertThat(param1.getValue()).isEqualTo("7.2");
            assertThat(param1.getUnit()).isEqualTo("10^3/uL");
            assertThat(param1.getRefRange()).isEqualTo("4.0-10.0");
            assertThat(param1.getFlag()).isEqualTo("N");

            // Verify second parameter
            TestResultParameterEventPayload param2 = parameters.get(1);
            assertThat(param2.getParamCode()).isEqualTo("RBC");
            assertThat(param2.getParamName()).isEqualTo("Red Blood Cells");
        }

        @Test
        @Order(4)
        @DisplayName("Should set correct event timestamps")
        void shouldSetCorrectEventTimestamps() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();

            assertThat(event.getEventTimestamp()).isNotNull();
            assertThat(event.getCreatedAt()).isNotNull();
            assertThat(event.getExpireAt()).isNotNull();

            // Verify eventTimestamp matches testResult createdAt
            assertThat(event.getEventTimestamp())
                    .isEqualTo(testResult.getCreatedAt().toInstant(ZoneOffset.UTC));
        }

        @Test
        @Order(5)
        @DisplayName("Should include appropriate tags in event")
        void shouldIncludeAppropriateTagsInEvent() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            List<String> tags = event.getTags();

            assertThat(tags).isNotNull();
            assertThat(tags).containsExactly("test-result", "ingestion", "BloodAnalyzer");
        }

        @Test
        @Order(6)
        @DisplayName("Should generate unique event ID for each event")
        void shouldGenerateUniqueEventIdForEachEvent() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When - Publish twice
            testResultEventPublisher.publishTestResultCreated(testResult);
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate, times(2)).send(anyString(), anyString(), eventCaptor.capture());

            List<MonitoringEventPayload> events = eventCaptor.getAllValues();
            assertThat(events.get(0).getEventId()).isNotEqualTo(events.get(1).getEventId());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // NULL AND EDGE CASE HANDLING
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Null and Edge Case Handling")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class NullAndEdgeCaseTests {

        @Test
        @Order(1)
        @DisplayName("Should handle null test result gracefully")
        void shouldHandleNullTestResultGracefully() {
            // When
            testResultEventPublisher.publishTestResultCreated(null);

            // Then - Should not throw exception, just log warning
            verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        }

        @Test
        @Order(2)
        @DisplayName("Should catch and log exception when test order is null")
        void shouldCatchAndLogExceptionWhenTestOrderIsNull() {
            // Given
            testResult.setTestOrder(null);

            // When - Exception should be caught internally and logged, not thrown
            assertThatCode(() -> testResultEventPublisher.publishTestResultCreated(testResult))
                    .doesNotThrowAnyException();

            // Then - Kafka should not be called because exception occurred during event building
            verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        }

        @Test
        @Order(3)
        @DisplayName("Should handle null instrument name with default value")
        void shouldHandleNullInstrumentNameWithDefaultValue() {
            // Given
            testResult.setInstrumentName(null);
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            assertThat(event.getPayload().get("instrumentName")).isEqualTo("Unknown");
            assertThat(event.getTags()).contains("Unknown");
        }

        @Test
        @Order(4)
        @DisplayName("Should handle null status with default value")
        void shouldHandleNullStatusWithDefaultValue() {
            // Given
            testResult.setStatus(null);
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            assertThat(event.getPayload().get("status")).isEqualTo("Unknown");
        }

        @Test
        @Order(5)
        @DisplayName("Should handle null parameters list with empty list")
        void shouldHandleNullParametersListWithEmptyList() {
            // Given
            testResult.setTestResultParameter(null);
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            @SuppressWarnings("unchecked")
            List<TestResultParameterEventPayload> parameters =
                    (List<TestResultParameterEventPayload>) event.getPayload().get("parameters");

            assertThat(parameters).isNotNull();
            assertThat(parameters).isEmpty();
        }

        @Test
        @Order(6)
        @DisplayName("Should handle empty parameters list")
        void shouldHandleEmptyParametersList() {
            // Given
            testResult.setTestResultParameter(Collections.emptyList());
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            @SuppressWarnings("unchecked")
            List<TestResultParameterEventPayload> parameters =
                    (List<TestResultParameterEventPayload>) event.getPayload().get("parameters");

            assertThat(parameters).isEmpty();
        }

        @Test
        @Order(7)
        @DisplayName("Should handle null createdAt with current timestamp")
        void shouldHandleNullCreatedAtWithCurrentTimestamp() {
            // Given
            testResult.setCreatedAt(null);
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            assertThat(event.getEventTimestamp()).isNotNull();
            // Should use current time (now) when createdAt is null
            assertThat(event.getEventTimestamp()).isCloseTo(
                    event.getCreatedAt(),
                    within(1000, java.time.temporal.ChronoUnit.MILLIS)
            );
        }

        @Test
        @Order(8)
        @DisplayName("Should allow null patient ID in payload")
        void shouldAllowNullPatientIdInPayload() {
            // Given
            testResult.setPatientId(null);
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            assertThat(event.getPayload().get("patientId")).isNull();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // KAFKA ERROR HANDLING
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Kafka Error Handling")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class KafkaErrorHandlingTests {

        @Test
        @Order(1)
        @DisplayName("Should handle Kafka send failure gracefully")
        void shouldHandleKafkaSendFailureGracefully() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = new CompletableFuture<>();
            future.completeExceptionally(new RuntimeException("Kafka connection failed"));
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When - Should not throw exception
            assertThatCode(() -> testResultEventPublisher.publishTestResultCreated(testResult))
                    .doesNotThrowAnyException();

            // Then
            verify(kafkaTemplate).send(anyString(), anyString(), any());
        }

        @Test
        @Order(2)
        @DisplayName("Should handle exception during event building")
        void shouldHandleExceptionDuringEventBuilding() {
            // Given - Create a test result that will cause an error during event building
            testResult.setTestOrder(null); // This will trigger IllegalStateException

            // When - Should catch the exception and log it
            assertThatCode(() -> testResultEventPublisher.publishTestResultCreated(testResult))
                    .doesNotThrowAnyException();

            // Then
            verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
        }

        @Test
        @Order(3)
        @DisplayName("Should continue processing after Kafka failure")
        void shouldContinueProcessingAfterKafkaFailure() {
            // Given
            CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RuntimeException("Network error"));

            CompletableFuture<SendResult<String, Object>> successFuture = CompletableFuture.completedFuture(null);

            when(kafkaTemplate.send(anyString(), anyString(), any()))
                    .thenReturn(failedFuture)
                    .thenReturn(successFuture);

            // When - First call fails, second should still work
            testResultEventPublisher.publishTestResultCreated(testResult);
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EVENT PAYLOAD VALIDATION
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Event Payload Validation")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EventPayloadValidationTests {

        @Test
        @Order(1)
        @DisplayName("Should use blood collection ID as correlation ID")
        void shouldUseBloodCollectionIdAsCorrelationId() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            assertThat(event.getCorrelationId()).isEqualTo(testResult.getBloodCollectionId());
        }

        @Test
        @Order(2)
        @DisplayName("Should set expireAt to 90 days from creation")
        void shouldSetExpireAtTo90DaysFromCreation() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();

            long daysDiff = java.time.Duration.between(event.getCreatedAt(), event.getExpireAt()).toDays();
            assertThat(daysDiff).isEqualTo(90);
        }

        @Test
        @Order(3)
        @DisplayName("Should use createdBy as operator ID")
        void shouldUseCreatedByAsOperatorId() {
            // Given
            testResult.setCreatedBy("Dr. John Doe");
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            assertThat(event.getOperatorId()).isEqualTo("Dr. John Doe");
        }

        @Test
        @Order(4)
        @DisplayName("Should map all test result parameters correctly")
        void shouldMapAllTestResultParametersCorrectly() {
            // Given
            TestResultParameter param3 = TestResultParameter.builder()
                    .paramCode("HGB")
                    .paramName("Hemoglobin")
                    .value("145")
                    .unit("g/dL")
                    .refRange("110.0-160.0")
                    .flag("N")
                    .build();

            testResult.getTestResultParameter().add(param3);

            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<MonitoringEventPayload> eventCaptor = ArgumentCaptor.forClass(MonitoringEventPayload.class);
            verify(kafkaTemplate).send(anyString(), anyString(), eventCaptor.capture());

            MonitoringEventPayload event = eventCaptor.getValue();
            @SuppressWarnings("unchecked")
            List<TestResultParameterEventPayload> parameters =
                    (List<TestResultParameterEventPayload>) event.getPayload().get("parameters");

            assertThat(parameters).hasSize(3);
            assertThat(parameters)
                    .extracting(TestResultParameterEventPayload::getParamCode)
                    .containsExactly("WBC", "RBC", "HGB");
        }

        @Test
        @Order(5)
        @DisplayName("Should send event with correct Kafka key")
        void shouldSendEventWithCorrectKafkaKey() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(anyString(), keyCaptor.capture(), any());

            // Key should be the correlation ID (blood collection ID)
            assertThat(keyCaptor.getValue()).isEqualTo("TUBE-001");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // INTEGRATION VERIFICATION
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integration Verification")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class IntegrationVerificationTests {

        @Test
        @Order(1)
        @DisplayName("Should only call Kafka send once per publish")
        void shouldOnlyCallKafkaSendOncePerPublish() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any());
        }

        @Test
        @Order(2)
        @DisplayName("Should not call ObjectMapper directly")
        void shouldNotCallObjectMapperDirectly() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then - ObjectMapper is injected but not used in current implementation
            verifyNoInteractions(objectMapper);
        }

        @Test
        @Order(3)
        @DisplayName("Should publish to correct topic")
        void shouldPublishToCorrectTopic() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);

            // Then
            ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(topicCaptor.capture(), anyString(), any());

            assertThat(topicCaptor.getValue()).isEqualTo("test-results-events");
        }

        @Test
        @Order(4)
        @DisplayName("Should handle multiple sequential publishes correctly")
        void shouldHandleMultipleSequentialPublishesCorrectly() {
            // Given
            CompletableFuture<SendResult<String, Object>> future = CompletableFuture.completedFuture(null);
            when(kafkaTemplate.send(anyString(), anyString(), any())).thenReturn(future);

            TestResult result2 = TestResult.builder()
                    .resultId("TR-002")
                    .testOrder(testOrder)
                    .bloodCollectionId("TUBE-002")
                    .instrumentName("Analyzer2")
                    .status("COMPLETED")
                    .build();
            result2.setCreatedAt(LocalDateTime.now());

            // When
            testResultEventPublisher.publishTestResultCreated(testResult);
            testResultEventPublisher.publishTestResultCreated(result2);

            // Then
            verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any());

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate, times(2)).send(anyString(), keyCaptor.capture(), any());

            List<String> keys = keyCaptor.getAllValues();
            assertThat(keys).containsExactly("TUBE-001", "TUBE-002");
        }
    }
}

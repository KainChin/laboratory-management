package com.example.test_order_service.unit.serviceImpl;

import com.example.test_order_service.dto.response.*;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.event.publisher.MonitoringEventPublisher;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.mapper.CommentMapper;
import com.example.test_order_service.mapper.TestOrderMapper;
import com.example.test_order_service.mapper.TestResultMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.serviceImpl.InstrumentSyncService;
import com.example.test_order_service.serviceImpl.TestOrderServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Tests for TestOrderServiceImpl
 * Tests business logic with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestOrderServiceImpl Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestOrderServiceImplTest {

    @Mock
    private TestOrderRepository testOrderRepository;

    @Mock
    private TestOrderMapper testOrderMapper;

    @Mock
    private TestResultMapper testResultMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private InstrumentSyncService instrumentSyncService;

    @Mock
    private MonitoringEventPublisher eventPublisher;

    @InjectMocks
    private TestOrderServiceImpl testOrderService;

    private TestOrderRequest testOrderRequest;
    private TestOrderUpdateRequest updateRequest;
    private TestOrder testOrder;
    private TestOrderResponse testOrderResponse;

    @BeforeEach
    void setUp() throws Exception {
        // Manually inject eventPublisher using reflection due to @Autowired(required = false)
        java.lang.reflect.Field field = TestOrderServiceImpl.class.getDeclaredField("eventPublisher");
        field.setAccessible(true);
        field.set(testOrderService, eventPublisher);

        testOrderRequest = TestOrderRequest.builder()
                .patientName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .citizenId("001234567890")
                .country("Vietnam")
                .gender(Gender.MALE)
                .address("123 Nguyen Hue, HCMC")
                .email("nguyenvana@example.com")
                .phone("0901234567")
                .build();

        updateRequest = TestOrderUpdateRequest.builder()
                .patientName("Nguyen Van B")
                .dateOfBirth(LocalDate.of(1992, 5, 20))
                .gender(Gender.MALE)
                .phone("0909876543")
                .address("456 Le Loi, HCMC")
                .email("nguyenvanb@example.com")
                .citizenId("009876543210")
                .country("Thailand")
                .status(TestOrderStatus.COMPLETED)
                .build();

        testOrder = TestOrder.builder()
                .testOrderId("TO-001")
                .patientName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .citizenId("001234567890")
                .country("Vietnam")
                .gender(Gender.MALE)
                .address("123 Nguyen Hue, HCMC")
                .email("nguyenvana@example.com")
                .phone("0901234567")
                .status(TestOrderStatus.PENDING)
                .build();
        testOrder.setCreatedBy("System");

        testOrderResponse = TestOrderResponse.builder()
                .testOrderId("TO-001")
                .patientName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .citizenId("001234567890")
                .country("Vietnam")
                .gender(Gender.MALE)
                .address("123 Nguyen Hue, HCMC")
                .email("nguyenvana@example.com")
                .phone("0901234567")
                .status(TestOrderStatus.PENDING)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // CREATE TEST ORDER TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Create Test Order Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateTestOrderTests {

        @Test
        @Order(1)
        @DisplayName("Should create test order successfully")
        void shouldCreateTestOrderSuccessfully() {
            // Given
            String dateCode = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            when(testOrderRepository.countByDateCode(dateCode)).thenReturn(0L);
            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            RestResponse<TestOrderResponse> response = testOrderService.createTestOrder(testOrderRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Test order created successfully");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getResult().getPatientName()).isEqualTo("Nguyen Van A");
            assertThat(response.getTimestamp()).isNotNull();

            verify(testOrderRepository).countByDateCode(dateCode);
            verify(testOrderMapper).toTestOrderEntity(testOrderRequest);
            verify(testOrderRepository).save(any(TestOrder.class));
            verify(testOrderMapper).toTestOrderResponse(testOrder);
        }

        @Test
        @Order(2)
        @DisplayName("Should set createdBy to System")
        void shouldSetCreatedByToSystem() {
            // Given
            String dateCode = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            when(testOrderRepository.countByDateCode(dateCode)).thenReturn(0L);
            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.createTestOrder(testOrderRequest);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    "System".equals(order.getCreatedBy())
            ));
        }

        @Test
        @Order(3)
        @DisplayName("Should create test order with country field")
        void shouldCreateTestOrderWithCountry() {
            // Given
            String dateCode = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            when(testOrderRepository.countByDateCode(dateCode)).thenReturn(0L);
            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            RestResponse<TestOrderResponse> response = testOrderService.createTestOrder(testOrderRequest);

            // Then
            assertThat(response.getResult().getCountry()).isEqualTo("Vietnam");
            verify(testOrderRepository).save(argThat(order ->
                    "Vietnam".equals(order.getCountry())
            ));
        }

        @Test
        @Order(4)
        @DisplayName("Should generate blood collection ID with correct format")
        void shouldGenerateBloodCollectionIdWithCorrectFormat() {
            // Given
            String dateCode = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            long currentCount = 5L;
            when(testOrderRepository.countByDateCode(dateCode)).thenReturn(currentCount);
            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.createTestOrder(testOrderRequest);

            // Then
            String expectedBloodCollectionId = String.format("BCT-%s-%05d", dateCode, currentCount + 1);
            verify(testOrderRepository).save(argThat(order ->
                    expectedBloodCollectionId.equals(order.getBloodCollectionId())
            ));
        }

        @Test
        @Order(5)
        @DisplayName("Should generate blood collection ID starting from 00001 when count is 0")
        void shouldGenerateBloodCollectionIdStartingFrom00001() {
            // Given
            String dateCode = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            when(testOrderRepository.countByDateCode(dateCode)).thenReturn(0L);
            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.createTestOrder(testOrderRequest);

            // Then
            String expectedBloodCollectionId = String.format("BCT-%s-00001", dateCode);
            verify(testOrderRepository).save(argThat(order ->
                    expectedBloodCollectionId.equals(order.getBloodCollectionId())
            ));
        }

        @Test
        @Order(4)
        @DisplayName("Should create test order with citizenId field")
        void shouldCreateTestOrderWithCitizenId() {
            // Given
            String dateCode = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            when(testOrderRepository.countByDateCode(dateCode)).thenReturn(0L);
            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            RestResponse<TestOrderResponse> response = testOrderService.createTestOrder(testOrderRequest);

            // Then
            assertThat(response.getResult().getCitizenId()).isEqualTo("001234567890");
            verify(testOrderRepository).save(argThat(order ->
                    "001234567890".equals(order.getCitizenId())
            ));
        }

        @Test
        @Order(5)
        @DisplayName("Should create test order with PENDING status by default")
        void shouldCreateTestOrderWithPendingStatus() {
            // Given
            String dateCode = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            when(testOrderRepository.countByDateCode(dateCode)).thenReturn(0L);
            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            RestResponse<TestOrderResponse> response = testOrderService.createTestOrder(testOrderRequest);

            // Then
            assertThat(response.getResult().getStatus()).isEqualTo(TestOrderStatus.PENDING);
            verify(testOrderRepository).save(argThat(order ->
                    TestOrderStatus.PENDING.equals(order.getStatus())
            ));
        }

        @Test
        @Order(6)
        @DisplayName("Should handle repository exception during create")
        void shouldHandleRepositoryExceptionDuringCreate() {
            // Given
            String dateCode = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            when(testOrderRepository.countByDateCode(dateCode)).thenReturn(0L);
            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> testOrderService.createTestOrder(testOrderRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");

            verify(testOrderRepository).save(any(TestOrder.class));
        }

        @Test
        @Order(7)
        @DisplayName("Should handle null mapper result gracefully")
        void shouldHandleNullMapperResultGracefully() {
            when(testOrderMapper.toTestOrderEntity(any())).thenReturn(null);

            // Assert exception
            assertThatThrownBy(() -> testOrderService.createTestOrder(testOrderRequest))
                    .isInstanceOf(NullPointerException.class);

            verify(testOrderMapper).toTestOrderEntity(any());
        }

        @Test
        @Order(8)
        @DisplayName("Should create with missing optional fields (email, phone)")
        void shouldCreateWithMissingOptionalFields() {
            String dateCode = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            when(testOrderRepository.countByDateCode(dateCode)).thenReturn(0L);
            testOrderRequest.setEmail(null);
            testOrderRequest.setPhone(null);
            when(testOrderMapper.toTestOrderEntity(any())).thenReturn(testOrder);
            when(testOrderRepository.save(any())).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any())).thenReturn(testOrderResponse);

            RestResponse<TestOrderResponse> res = testOrderService.createTestOrder(testOrderRequest);
            assertThat(res.getResult()).isNotNull();
            verify(testOrderRepository).save(any());
        }

        @Test
        @Order(9)
        @DisplayName("Should throw RuntimeException when save fails")
        void shouldThrowRuntimeExceptionWhenSaveFails() {
            // Given
            String dateCode = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
            when(testOrderRepository.countByDateCode(dateCode)).thenReturn(0L);
            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class)))
                    .thenThrow(new RuntimeException("Generic save error"));

            // When & Then
            assertThatThrownBy(() -> testOrderService.createTestOrder(testOrderRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Generic save error");
        }

        @Test
        @Order(10)
        @DisplayName("Should throw IllegalArgumentException when request is null")
        void shouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
            // When & Then
            assertThatThrownBy(() -> testOrderService.createTestOrder(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("No test results provided");

            verify(testOrderRepository, never()).save(any());
        }

    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE TEST ORDER TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Update Test Order Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdateTestOrderTests {

        @Test
        @Order(7)
        @DisplayName("Should update test order successfully with all fields")
        void shouldUpdateTestOrderSuccessfully() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            RestResponse<TestOrderResponse> response = testOrderService.updateTestOrder(orderId, updateRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Test order updated successfully");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getTimestamp()).isNotNull();

            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository).save(any(TestOrder.class));
            verify(testOrderMapper).toTestOrderResponse(any(TestOrder.class));
        }

        @Test
        @Order(8)
        @DisplayName("Should update only provided fields (partial update)")
        void shouldUpdateOnlyProvidedFields() {
            // Given
            String orderId = "TO-001";
            TestOrderUpdateRequest partialUpdate = new TestOrderUpdateRequest();
            partialUpdate.setPatientName("New Name");
            // Other fields are null - should preserve existing values

            String originalPhone = testOrder.getPhone();
            String originalEmail = testOrder.getEmail();
            Gender originalGender = testOrder.getGender();

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, partialUpdate);

            // Then - Only patientName should be updated, others preserved
            verify(testOrderRepository).save(argThat(order ->
                    "New Name".equals(order.getPatientName()) &&
                            originalGender.equals(order.getGender()) &&
                            originalPhone.equals(order.getPhone()) &&
                            originalEmail.equals(order.getEmail())
            ));
        }

        @Test
        @Order(9)
        @DisplayName("Should update country when provided")
        void shouldUpdateCountryWhenProvided() {
            // Given
            String orderId = "TO-001";
            String newCountry = "Thailand";
            TestOrderUpdateRequest updateWithCountry = new TestOrderUpdateRequest();
            updateWithCountry.setCountry(newCountry);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, updateWithCountry);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    newCountry.equals(order.getCountry())
            ));
        }

        @Test
        @Order(10)
        @DisplayName("Should preserve country when not provided in update")
        void shouldPreserveCountryWhenNotProvided() {
            // Given
            String orderId = "TO-001";
            String originalCountry = testOrder.getCountry();
            TestOrderUpdateRequest partialUpdate = new TestOrderUpdateRequest();
            partialUpdate.setPatientName("New Name");
            // country is null

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, partialUpdate);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    originalCountry.equals(order.getCountry())
            ));
        }

        @Test
        @Order(11)
        @DisplayName("Should update status when provided")
        void shouldUpdateStatusWhenProvided() {
            // Given
            String orderId = "TO-001";
            TestOrderStatus newStatus = TestOrderStatus.COMPLETED;
            TestOrderUpdateRequest updateWithStatus = new TestOrderUpdateRequest();
            updateWithStatus.setStatus(newStatus);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, updateWithStatus);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    newStatus.equals(order.getStatus())
            ));
        }

        @Test
        @Order(12)
        @DisplayName("Should preserve status when not provided in update")
        void shouldPreserveStatusWhenNotProvided() {
            // Given
            String orderId = "TO-001";
            TestOrderStatus originalStatus = testOrder.getStatus();
            TestOrderUpdateRequest partialUpdate = new TestOrderUpdateRequest();
            partialUpdate.setPatientName("New Name");
            // status is null

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, partialUpdate);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    originalStatus.equals(order.getStatus())
            ));
        }

        @Test
        @Order(13)
        @DisplayName("Should update citizenId when provided")
        void shouldUpdateCitizenIdWhenProvided() {
            // Given
            String orderId = "TO-001";
            String newCitizenId = "999888777666";
            TestOrderUpdateRequest updateWithCitizenId = new TestOrderUpdateRequest();
            updateWithCitizenId.setCitizenId(newCitizenId);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, updateWithCitizenId);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    newCitizenId.equals(order.getCitizenId())
            ));
        }

        @Test
        @Order(14)
        @DisplayName("Should preserve citizenId when not provided in update")
        void shouldPreserveCitizenIdWhenNotProvided() {
            // Given
            String orderId = "TO-001";
            String originalCitizenId = testOrder.getCitizenId();
            TestOrderUpdateRequest partialUpdate = new TestOrderUpdateRequest();
            partialUpdate.setPatientName("New Name");
            // citizenId is null

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, partialUpdate);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    originalCitizenId.equals(order.getCitizenId())
            ));
        }

        @Test
        @Order(15)
        @DisplayName("Should update country, status, and citizenId together")
        void shouldUpdateCountryStatusAndCitizenIdTogether() {
            // Given
            String orderId = "TO-001";
            TestOrderUpdateRequest fullUpdate = TestOrderUpdateRequest.builder()
                    .country("Singapore")
                    .status(TestOrderStatus.COMPLETED)  // ✅ Thay đổi từ IN_PROGRESS
                    .citizenId("111222333444")
                    .build();

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, fullUpdate);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    "Singapore".equals(order.getCountry()) &&
                            TestOrderStatus.COMPLETED.equals(order.getStatus()) &&  // ✅ Thay đổi
                            "111222333444".equals(order.getCitizenId())
            ));
        }

        @Test
        @Order(16)
        @DisplayName("Should throw ResourceNotFoundException when test order not found")
        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
            // Given
            String invalidOrderId = "INVALID-ID";
            when(testOrderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testOrderService.updateTestOrder(invalidOrderId, updateRequest))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(invalidOrderId);
            verify(testOrderRepository, never()).save(any(TestOrder.class));
            verify(testOrderMapper, never()).toTestOrderResponse(any(TestOrder.class));
        }

        @Test
        @Order(17)
        @DisplayName("Should preserve all fields when update request has all null values")
        void shouldPreserveAllFieldsWhenUpdateHasAllNullValues() {
            // Given
            String orderId = "TO-001";
            TestOrderUpdateRequest emptyUpdate = new TestOrderUpdateRequest();
            // All fields are null

            String originalName = testOrder.getPatientName();
            String originalPhone = testOrder.getPhone();
            String originalEmail = testOrder.getEmail();
            String originalAddress = testOrder.getAddress();
            String originalCitizenId = testOrder.getCitizenId();
            String originalCountry = testOrder.getCountry();
            TestOrderStatus originalStatus = testOrder.getStatus();

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, emptyUpdate);

            // Then - All original values should be preserved
            verify(testOrderRepository).save(argThat(order ->
                    originalName.equals(order.getPatientName()) &&
                            originalPhone.equals(order.getPhone()) &&
                            originalEmail.equals(order.getEmail()) &&
                            originalAddress.equals(order.getAddress()) &&
                            originalCitizenId.equals(order.getCitizenId()) &&
                            originalCountry.equals(order.getCountry()) &&
                            originalStatus.equals(order.getStatus())
            ));
        }

        @Test
        @Order(18)
        @DisplayName("Should handle repository exception during update")
        void shouldHandleRepositoryExceptionDuringUpdate() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> testOrderService.updateTestOrder(orderId, updateRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");
        }

        @Test
        @DisplayName("Should handle null updateRequest")
        void shouldHandleNullUpdateRequest() {
            String id = "TO-001";
            when(testOrderRepository.findById(id)).thenReturn(Optional.of(testOrder));
            assertThatThrownBy(() -> testOrderService.updateTestOrder(id, null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should throw exception if save operation fails")
        void shouldThrowIfSaveFails() {
            String id = "TO-001";
            when(testOrderRepository.findById(id)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any())).thenThrow(new DataAccessException("Database error") {
            });

            assertThatThrownBy(() -> testOrderService.updateTestOrder(id, updateRequest))
                    .isInstanceOf(DataAccessException.class)
                    .hasMessageContaining("Database error");
        }

        @Test
        @DisplayName("Should not update when updateRequest equals current entity")
        void shouldNotUpdateWhenNoChangesDetected() {
            String id = "TO-001";
            TestOrderUpdateRequest identical = TestOrderUpdateRequest.builder()
                    .patientName("Nguyen Van A")
                    .country("Vietnam")
                    .citizenId("001234567890")
                    .build();

            when(testOrderRepository.findById(id)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any())).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any())).thenReturn(testOrderResponse);

            RestResponse<TestOrderResponse> res = testOrderService.updateTestOrder(id, identical);
            assertThat(res.getStatusCode()).isEqualTo(200);
            verify(testOrderRepository).save(any());
        }

        @Test
        @DisplayName("Should publish status change event on status update")
        void shouldPublishStatusChangeEventOnStatusUpdate() {
            // Given
            String orderId = "TO-001";
            testOrder.setStatus(TestOrderStatus.PENDING);
            TestOrderUpdateRequest statusUpdate = TestOrderUpdateRequest.builder()
                    .status(TestOrderStatus.COMPLETED)
                    .build();

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenAnswer(inv -> inv.getArgument(0));
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, statusUpdate);

            // Then
            verify(eventPublisher).publishStatusChanged(orderId, TestOrderStatus.PENDING, TestOrderStatus.COMPLETED);
        }

        @Test
        @DisplayName("Should not publish event if status is not changed")
        void shouldNotPublishEventIfStatusIsNotChanged() {
            // Given
            String orderId = "TO-001";
            testOrder.setStatus(TestOrderStatus.PENDING);
            TestOrderUpdateRequest statusUpdate = TestOrderUpdateRequest.builder()
                    .status(TestOrderStatus.PENDING) // Same status
                    .build();

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, statusUpdate);

            // Then
            verify(eventPublisher, never()).publishStatusChanged(anyString(), any(), any());
        }

        @Test
        @DisplayName("Should not publish event if status is null in request")
        void shouldNotPublishEventIfStatusIsNullInRequest() {
            // Given
            String orderId = "TO-001";
            testOrder.setStatus(TestOrderStatus.PENDING);
            TestOrderUpdateRequest noStatusUpdate = TestOrderUpdateRequest.builder()
                    .patientName("Some Name") // Other field update
                    .build();

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, noStatusUpdate);

            // Then
            verify(eventPublisher, never()).publishStatusChanged(anyString(), any(), any());
        }

        @Test
        @DisplayName("Should not publish event if publisher is null")
        void shouldNotPublishEventIfPublisherIsNull() {
            // Given
            TestOrderServiceImpl serviceWithNullPublisher = new TestOrderServiceImpl(
                    testOrderRepository, testOrderMapper, commentMapper, instrumentSyncService
            );
            String orderId = "TO-001";
            testOrder.setStatus(TestOrderStatus.PENDING);
            TestOrderUpdateRequest statusUpdate = TestOrderUpdateRequest.builder()
                    .status(TestOrderStatus.COMPLETED)
                    .build();

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            serviceWithNullPublisher.updateTestOrder(orderId, statusUpdate);

            // Then
            // No exception should be thrown, and no event published.
            // Verification on a null object is not possible, the absence of a NullPointerException is the test.
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GET TEST ORDERS TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Test Orders Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetTestOrdersTests {

        @Test
        @Order(19)
        @DisplayName("Should return paginated test orders")
        void shouldReturnPaginatedTestOrders() {
            // Given
            TestOrder order1 = TestOrder.builder()
                    .testOrderId("TO-001")
                    .patientName("Patient 1")
                    .country("Vietnam")
                    .citizenId("001111111111")
                    .status(TestOrderStatus.PENDING)
                    .build();

            TestOrder order2 = TestOrder.builder()
                    .testOrderId("TO-002")
                    .patientName("Patient 2")
                    .country("Thailand")
                    .citizenId("002222222222")
                    .status(TestOrderStatus.COMPLETED)
                    .build();

            Page<TestOrder> testOrderPage = new PageImpl<>(
                    Arrays.asList(order1, order2),
                    PageRequest.of(0, 6),
                    2
            );

            TestOrderResponse response1 = TestOrderResponse.builder()
                    .testOrderId("TO-001")
                    .patientName("Patient 1")
                    .country("Vietnam")
                    .citizenId("001111111111")
                    .status(TestOrderStatus.PENDING)
                    .build();

            TestOrderResponse response2 = TestOrderResponse.builder()
                    .testOrderId("TO-002")
                    .patientName("Patient 2")
                    .country("Thailand")
                    .citizenId("002222222222")
                    .status(TestOrderStatus.COMPLETED)
                    .build();

            when(testOrderRepository.findTestOrdersByParams(any(Pageable.class), anyString()))
                    .thenReturn(testOrderPage);

            // Mock dựa trên testOrderId thay vì object identity
            when(testOrderMapper.toTestOrderResponse(argThat(order ->
                    order != null && "TO-001".equals(order.getTestOrderId())
            ))).thenReturn(response1);

            when(testOrderMapper.toTestOrderResponse(argThat(order ->
                    order != null && "TO-002".equals(order.getTestOrderId())
            ))).thenReturn(response2);

            // When
            Pageable pageable = PageRequest.of(0, 6);
            PageResponse<TestOrderResponse> response = testOrderService.getTestOrders(pageable, "");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCurrentPage()).isEqualTo(1);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getItems()).hasSize(2);

            // Kiểm tra thứ tự
            assertThat(response.getItems().get(0).getTestOrderId()).isEqualTo("TO-001");
            assertThat(response.getItems().get(0).getCountry()).isEqualTo("Vietnam");
            assertThat(response.getItems().get(0).getStatus()).isEqualTo(TestOrderStatus.PENDING);
            assertThat(response.getItems().get(1).getTestOrderId()).isEqualTo("TO-002");
            assertThat(response.getItems().get(1).getCountry()).isEqualTo("Thailand");
            assertThat(response.getItems().get(1).getStatus()).isEqualTo(TestOrderStatus.COMPLETED);

            verify(testOrderRepository).findTestOrdersByParams(pageable, "");
            verify(testOrderMapper, times(2)).toTestOrderResponse(any(TestOrder.class));
        }

        @Test
        @Order(20)
        @DisplayName("Should filter test orders by keyword")
        void shouldFilterTestOrdersByKeyword() {
            // Given
            String keyword = "Nguyen";
            Page<TestOrder> testOrderPage = new PageImpl<>(
                    Collections.singletonList(testOrder),
                    PageRequest.of(0, 6),
                    1
            );

            when(testOrderRepository.findTestOrdersByParams(any(Pageable.class), eq(keyword)))
                    .thenReturn(testOrderPage);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            Pageable pageable = PageRequest.of(0, 6);
            PageResponse<TestOrderResponse> response = testOrderService.getTestOrders(pageable, keyword);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getPatientName()).contains("Nguyen");

            verify(testOrderRepository).findTestOrdersByParams(pageable, keyword);
        }

        @Test
        @Order(21)
        @DisplayName("Should return empty list when no test orders found")
        void shouldReturnEmptyListWhenNoTestOrdersFound() {
            // Given
            Page<TestOrder> emptyPage = new PageImpl<>(
                    List.of(),
                    PageRequest.of(0, 6),
                    0
            );

            when(testOrderRepository.findTestOrdersByParams(any(Pageable.class), anyString()))
                    .thenReturn(emptyPage);

            // When
            Pageable pageable = PageRequest.of(0, 6);
            PageResponse<TestOrderResponse> response = testOrderService.getTestOrders(pageable, "");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getItems()).isEmpty();
            assertThat(response.getTotalPages()).isEqualTo(0);
            assertThat(response.getCurrentPage()).isEqualTo(1);
        }

        @Test
        @Order(22)
        @DisplayName("Should handle pagination correctly for multiple pages")
        void shouldHandlePaginationCorrectlyForMultiplePages() {
            // Given - Page 2 of 3
            Page<TestOrder> testOrderPage = new PageImpl<>(
                    Collections.singletonList(testOrder),
                    PageRequest.of(1, 6), // Page index 1 = Page 2
                    18 // Total 18 items, 3 pages
            );

            when(testOrderRepository.findTestOrdersByParams(any(Pageable.class), anyString()))
                    .thenReturn(testOrderPage);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            Pageable pageable = PageRequest.of(1, 6);
            PageResponse<TestOrderResponse> response = testOrderService.getTestOrders(pageable, "");

            // Then
            assertThat(response.getCurrentPage()).isEqualTo(2);
            assertThat(response.getTotalPages()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should handle null keyword gracefully")
        void shouldHandleNullKeywordGracefully() {
            Page<TestOrder> page = new PageImpl<>(List.of(testOrder), PageRequest.of(0, 6), 1);
            when(testOrderRepository.findTestOrdersByParams(any(), isNull())).thenReturn(page);
            when(testOrderMapper.toTestOrderResponse(any())).thenReturn(testOrderResponse);

            var res = testOrderService.getTestOrders(PageRequest.of(0, 6), null);
            assertThat(res.getItems()).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle repository returning null page")
        void shouldHandleRepositoryReturningNullPage() {
            when(testOrderRepository.findTestOrdersByParams(any(), any())).thenReturn(null);
            assertThatThrownBy(() -> testOrderService.getTestOrders(PageRequest.of(0, 6), ""))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Should handle empty keyword and null mapping result")
        void shouldHandleNullMappingResult() {
            Page<TestOrder> page = new PageImpl<>(List.of(testOrder), PageRequest.of(0, 6), 1);
            when(testOrderRepository.findTestOrdersByParams(any(), any())).thenReturn(page);
            when(testOrderMapper.toTestOrderResponse(any())).thenReturn(null);

            var res = testOrderService.getTestOrders(PageRequest.of(0, 6), "");
            assertThat(res.getItems()).containsNull();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE TEST ORDER TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Delete Test Order Tests (Soft Delete)")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteTestOrderTests {

        @Test
        @Order(23)
        @DisplayName("Should soft delete test order successfully and return updated page")
        void shouldSoftDeleteTestOrderSuccessfullyAndReturnUpdatedPage() {
            // Given
            String orderId = "TO-001";
            Pageable pageable = PageRequest.of(0, 6);
            String keyword = "";

            testOrder.setDeleted(false);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);

            Page<TestOrder> mockPage = new PageImpl<>(List.of(testOrder), pageable, 1);
            when(testOrderRepository.findTestOrdersByParams(pageable, keyword)).thenReturn(mockPage);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            var response = testOrderService.deleteTestOrder(orderId, pageable, keyword);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getItems()).hasSize(1);
            assertThat(response.getItems().get(0).getTestOrderId()).isEqualTo("TO-001");
            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository).save(argThat(order -> order.isDeleted()));
            verify(testOrderRepository).findTestOrdersByParams(pageable, keyword);
        }

        @Test
        @Order(24)
        @DisplayName("Should throw ResourceNotFoundException when test order not found for soft delete")
        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
            // Given
            String invalidOrderId = "INVALID-ID";
            Pageable pageable = PageRequest.of(0, 6);
            String keyword = "";

            when(testOrderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testOrderService.deleteTestOrder(invalidOrderId, pageable, keyword))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(invalidOrderId);
            verify(testOrderRepository, never()).save(any(TestOrder.class));
        }

        @Test
        @Order(25)
        @DisplayName("Should handle repository exception during soft delete")
        void shouldHandleRepositoryExceptionDuringSoftDelete() {
            // Given
            String orderId = "TO-001";
            Pageable pageable = PageRequest.of(0, 6);
            String keyword = "";

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> testOrderService.deleteTestOrder(orderId, pageable, keyword))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");

            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository).save(any(TestOrder.class));
        }

        void shouldHandleAlreadyDeletedOrderGracefully() {
            String id = "TO-001";
            Pageable pageable = PageRequest.of(0, 6);
            testOrder.setDeleted(true);

            when(testOrderRepository.findById(id)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any())).thenReturn(testOrder);
            when(testOrderRepository.findTestOrdersByParams(pageable, "")).thenReturn(Page.empty());
            when(testOrderMapper.toTestOrderResponse(any())).thenReturn(testOrderResponse);

            PageResponse<TestOrderResponse> res = testOrderService.deleteTestOrder(id, pageable, "");
            assertThat(res.getItems()).isEmpty();
        }
    }


    // ═══════════════════════════════════════════════════════════════
// GET TEST ORDER BY ID TESTS
// ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Test Order By ID Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetTestOrderByIdTests {

        private TestOrderDetailResponse testOrderDetailResponse;
        private Comment comment1;
        private Comment comment2;
        private CommentResponse commentResponse1;
        private CommentResponse commentResponse2;

        @BeforeEach
        void setUpDetailTests() {
            // Setup comments with different created times
            comment1 = Comment.builder()
                    .commentId("C-001")
                    .commentText("First observation")
                    .build();
            comment1.setCreatedBy("Doctor A");
            comment1.setCreatedAt(LocalDateTime.of(2024, 1, 2, 9, 0));

            comment2 = Comment.builder()
                    .commentId("C-002")
                    .commentText("Follow-up needed")
                    .build();
            comment2.setCreatedBy("Doctor B");
            comment2.setCreatedAt(LocalDateTime.of(2024, 1, 2, 10, 0));

            commentResponse1 = CommentResponse.builder()
                    .commentId("C-001")
                    .commentText("First observation")
                    .createdBy("Doctor A")
                    .build();

            commentResponse2 = CommentResponse.builder()
                    .commentId("C-002")
                    .commentText("Follow-up needed")
                    .createdBy("Doctor B")
                    .build();

            testOrderDetailResponse = TestOrderDetailResponse.builder()
                    .testOrderId("TO-001")
                    .patientName("Nguyen Van A")
                    .dateOfBirth(LocalDate.of(1990, 1, 15))
                    .age(35)
                    .citizenId("001234567890")
                    .country("Vietnam")
                    .gender(Gender.MALE)
                    .phone("0901234567")
                    .address("123 Nguyen Hue, HCMC")
                    .email("nguyenvana@example.com")
                    .status(TestOrderStatus.PENDING)
                    .build();

            // Add comments to test order in unsorted order
            testOrder.setComments(Arrays.asList(comment2, comment1));
        }

        @Test
        @Order(1)
        @DisplayName("Should return test order detail successfully")
        void shouldReturnTestOrderDetailSuccessfully() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderMapper.toTestOrderDetailResponse(testOrder)).thenReturn(testOrderDetailResponse);
            when(commentMapper.toCommentResponse(comment1)).thenReturn(commentResponse1);
            when(commentMapper.toCommentResponse(comment2)).thenReturn(commentResponse2);

            // When
            RestResponse<TestOrderDetailResponse> response = testOrderService.getTestOrderById(orderId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Test order retrieved successfully");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getResult().getTestOrderId()).isEqualTo("TO-001");
            assertThat(response.getResult().getPatientName()).isEqualTo("Nguyen Van A");
            assertThat(response.getTimestamp()).isNotNull();

            verify(testOrderRepository).findById(orderId);
            verify(testOrderMapper).toTestOrderDetailResponse(testOrder);
        }

        @Test
        @Order(2)
        @DisplayName("Should throw ResourceNotFoundException when order not found")
        void shouldThrowResourceNotFoundExceptionWhenOrderNotFound() {
            // Given
            String invalidOrderId = "INVALID-ID";
            when(testOrderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testOrderService.getTestOrderById(invalidOrderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(invalidOrderId);
            verify(testOrderMapper, never()).toTestOrderDetailResponse(any());
        }

        @Test
        @Order(3)
        @DisplayName("Should calculate age correctly from date of birth")
        void shouldCalculateAgeCorrectly() {
            // Given
            String orderId = "TO-001";
            LocalDate dob = LocalDate.of(1990, 1, 15);
            testOrder.setDateOfBirth(dob);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderMapper.toTestOrderDetailResponse(testOrder)).thenReturn(testOrderDetailResponse);
            when(commentMapper.toCommentResponse(any())).thenReturn(commentResponse1);

            // When
            RestResponse<TestOrderDetailResponse> response = testOrderService.getTestOrderById(orderId);

            // Then
            assertThat(response.getResult().getAge()).isNotNull();
            // Age calculation is handled by GeneralUtils.calculateAge()
            verify(testOrderRepository).findById(orderId);
        }

        @Test
        @Order(4)
        @DisplayName("Should sort comments by createdAt ascending")
        void shouldSortCommentsByCreatedAtAscending() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderMapper.toTestOrderDetailResponse(testOrder)).thenReturn(testOrderDetailResponse);
            when(commentMapper.toCommentResponse(comment1)).thenReturn(commentResponse1);
            when(commentMapper.toCommentResponse(comment2)).thenReturn(commentResponse2);

            // When
            RestResponse<TestOrderDetailResponse> response = testOrderService.getTestOrderById(orderId);

            // Then - Verify mapper was called in sorted order (comment1 created before comment2)
            verify(commentMapper).toCommentResponse(comment1);
            verify(commentMapper).toCommentResponse(comment2);

            // Verify the comments are in the response
            assertThat(response.getResult().getComments()).isNotNull();
        }

        @Test
        @Order(5)
        @DisplayName("Should handle empty comments list")
        void shouldHandleEmptyCommentsList() {
            // Given
            String orderId = "TO-001";
            testOrder.setComments(Collections.emptyList());

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderMapper.toTestOrderDetailResponse(testOrder)).thenReturn(testOrderDetailResponse);

            // When
            RestResponse<TestOrderDetailResponse> response = testOrderService.getTestOrderById(orderId);

            // Then
            assertThat(response).isNotNull();
            verify(commentMapper, never()).toCommentResponse(any());
        }

        @Test
        @Order(6)
        @DisplayName("Should handle null comments list gracefully")
        void shouldHandleNullCommentsList() {
            // Given
            String orderId = "TO-001";
            testOrder.setComments(null);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderMapper.toTestOrderDetailResponse(testOrder)).thenReturn(testOrderDetailResponse);

            // When & Then
            assertThatThrownBy(() -> testOrderService.getTestOrderById(orderId))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @Order(7)
        @DisplayName("Should handle repository exception")
        void shouldHandleRepositoryException() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> testOrderService.getTestOrderById(orderId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");
        }

        @Test
        @Order(12)
        @DisplayName("Should handle mapper exception")
        void shouldHandleMapperException() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderMapper.toTestOrderDetailResponse(testOrder))
                    .thenThrow(new RuntimeException("Mapping error"));

            // When & Then
            assertThatThrownBy(() -> testOrderService.getTestOrderById(orderId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Mapping error");
        }
    }

// ═══════════════════════════════════════════════════════════════
// GET TEST ORDER STATISTICS TESTS
// ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Test Order Statistics Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetTestOrderStatisticsTests {

        @Test
        @Order(1)
        @DisplayName("Should return statistics with all statuses")
        void shouldReturnStatisticsWithAllStatuses() {
            // Given
            long totalCount = 100L;
            List<Object[]> groupedCounts = Arrays.asList(
                    new Object[]{"PENDING", 20L},
                    new Object[]{"COMPLETED", 30L},
                    new Object[]{"CANCELLED", 10L},
                    new Object[]{"REVIEWED", 25L},
                    new Object[]{"AI_REVIEWED", 15L}
            );

            when(testOrderRepository.countActive()).thenReturn(totalCount);
            when(testOrderRepository.countByStatus()).thenReturn(groupedCounts);

            // When
            RestResponse<TestOrderStatisticResponse> response = testOrderService.getTestOrderStatistics();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Statistics retrieved successfully");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getTimestamp()).isNotNull();

            TestOrderStatisticResponse stats = response.getResult();
            assertThat(stats.getTotal()).isEqualTo(100L);
            assertThat(stats.getPending()).isEqualTo(20L);
            assertThat(stats.getCompleted()).isEqualTo(30L);
            assertThat(stats.getCancelled()).isEqualTo(10L);
            assertThat(stats.getReviewed()).isEqualTo(25L);
            assertThat(stats.getAiReviewed()).isEqualTo(15L);

            verify(testOrderRepository).countActive();
            verify(testOrderRepository).countByStatus();
        }

        @Test
        @Order(2)
        @DisplayName("Should handle missing statuses with default zero values")
        void shouldHandleMissingStatusesWithDefaultZero() {
            // Given - Only PENDING and COMPLETED statuses exist
            long totalCount = 50L;
            List<Object[]> groupedCounts = Arrays.asList(
                    new Object[]{"PENDING", 30L},
                    new Object[]{"COMPLETED", 20L}
                    // CANCELLED, REVIEWED, AI_REVIEWED are missing
            );

            when(testOrderRepository.countActive()).thenReturn(totalCount);
            when(testOrderRepository.countByStatus()).thenReturn(groupedCounts);

            // When
            RestResponse<TestOrderStatisticResponse> response = testOrderService.getTestOrderStatistics();

            // Then
            TestOrderStatisticResponse stats = response.getResult();
            assertThat(stats.getTotal()).isEqualTo(50L);
            assertThat(stats.getPending()).isEqualTo(30L);
            assertThat(stats.getCompleted()).isEqualTo(20L);
            assertThat(stats.getCancelled()).isEqualTo(0L); // Default
            assertThat(stats.getReviewed()).isEqualTo(0L); // Default
            assertThat(stats.getAiReviewed()).isEqualTo(0L); // Default
        }

        @Test
        @Order(3)
        @DisplayName("Should handle empty grouped counts")
        void shouldHandleEmptyGroupedCounts() {
            // Given
            long totalCount = 0L;
            List<Object[]> emptyGroupedCounts = Collections.emptyList();

            when(testOrderRepository.countActive()).thenReturn(totalCount);
            when(testOrderRepository.countByStatus()).thenReturn(emptyGroupedCounts);

            // When
            RestResponse<TestOrderStatisticResponse> response = testOrderService.getTestOrderStatistics();

            // Then
            TestOrderStatisticResponse stats = response.getResult();
            assertThat(stats.getTotal()).isEqualTo(0L);
            assertThat(stats.getPending()).isEqualTo(0L);
            assertThat(stats.getCompleted()).isEqualTo(0L);
            assertThat(stats.getCancelled()).isEqualTo(0L);
            assertThat(stats.getReviewed()).isEqualTo(0L);
            assertThat(stats.getAiReviewed()).isEqualTo(0L);
        }

        @Test
        @Order(4)
        @DisplayName("Should return correct counts for each status")
        void shouldReturnCorrectCountsForEachStatus() {
            // Given
            long totalCount = 5L;
            List<Object[]> groupedCounts = Arrays.asList(
                    new Object[]{"PENDING", 1L},
                    new Object[]{"COMPLETED", 1L},
                    new Object[]{"CANCELLED", 1L},
                    new Object[]{"REVIEWED", 1L},
                    new Object[]{"AI_REVIEWED", 1L}
            );

            when(testOrderRepository.countActive()).thenReturn(totalCount);
            when(testOrderRepository.countByStatus()).thenReturn(groupedCounts);

            // When
            RestResponse<TestOrderStatisticResponse> response = testOrderService.getTestOrderStatistics();

            // Then
            TestOrderStatisticResponse stats = response.getResult();
            assertThat(stats.getTotal()).isEqualTo(5L);
            assertThat(stats.getPending()).isEqualTo(1L);
            assertThat(stats.getCompleted()).isEqualTo(1L);
            assertThat(stats.getCancelled()).isEqualTo(1L);
            assertThat(stats.getReviewed()).isEqualTo(1L);
            assertThat(stats.getAiReviewed()).isEqualTo(1L);
        }

        @Test
        @Order(5)
        @DisplayName("Should handle only PENDING status")
        void shouldHandleOnlyPendingStatus() {
            // Given
            long totalCount = 50L;
            List<Object[]> groupedCounts = Collections.singletonList(
                    new Object[]{"PENDING", 50L}
            );

            when(testOrderRepository.countActive()).thenReturn(totalCount);
            when(testOrderRepository.countByStatus()).thenReturn(groupedCounts);

            // When
            RestResponse<TestOrderStatisticResponse> response = testOrderService.getTestOrderStatistics();

            // Then
            TestOrderStatisticResponse stats = response.getResult();
            assertThat(stats.getTotal()).isEqualTo(50L);
            assertThat(stats.getPending()).isEqualTo(50L);
            assertThat(stats.getCompleted()).isEqualTo(0L);
            assertThat(stats.getCancelled()).isEqualTo(0L);
            assertThat(stats.getReviewed()).isEqualTo(0L);
            assertThat(stats.getAiReviewed()).isEqualTo(0L);
        }

        @Test
        @Order(6)
        @DisplayName("Should handle only COMPLETED status")
        void shouldHandleOnlyCompletedStatus() {
            // Given
            long totalCount = 75L;
            List<Object[]> groupedCounts = Collections.singletonList(
                    new Object[]{"COMPLETED", 75L}
            );

            when(testOrderRepository.countActive()).thenReturn(totalCount);
            when(testOrderRepository.countByStatus()).thenReturn(groupedCounts);

            // When
            RestResponse<TestOrderStatisticResponse> response = testOrderService.getTestOrderStatistics();

            // Then
            TestOrderStatisticResponse stats = response.getResult();
            assertThat(stats.getTotal()).isEqualTo(75L);
            assertThat(stats.getPending()).isEqualTo(0L);
            assertThat(stats.getCompleted()).isEqualTo(75L);
            assertThat(stats.getCancelled()).isEqualTo(0L);
            assertThat(stats.getReviewed()).isEqualTo(0L);
            assertThat(stats.getAiReviewed()).isEqualTo(0L);
        }

        @Test
        @Order(7)
        @DisplayName("Should handle large count values")
        void shouldHandleLargeCountValues() {
            // Given
            long totalCount = 999999L;
            List<Object[]> groupedCounts = Arrays.asList(
                    new Object[]{"PENDING", 200000L},
                    new Object[]{"COMPLETED", 500000L},
                    new Object[]{"CANCELLED", 100000L},
                    new Object[]{"REVIEWED", 150000L},
                    new Object[]{"AI_REVIEWED", 49999L}
            );

            when(testOrderRepository.countActive()).thenReturn(totalCount);
            when(testOrderRepository.countByStatus()).thenReturn(groupedCounts);

            // When
            RestResponse<TestOrderStatisticResponse> response = testOrderService.getTestOrderStatistics();

            // Then
            TestOrderStatisticResponse stats = response.getResult();
            assertThat(stats.getTotal()).isEqualTo(999999L);
            assertThat(stats.getPending()).isEqualTo(200000L);
            assertThat(stats.getCompleted()).isEqualTo(500000L);
            assertThat(stats.getCancelled()).isEqualTo(100000L);
            assertThat(stats.getReviewed()).isEqualTo(150000L);
            assertThat(stats.getAiReviewed()).isEqualTo(49999L);
        }

        @Test
        @Order(8)
        @DisplayName("Should handle repository exception for countActive")
        void shouldHandleRepositoryExceptionForCountActive() {
            // Given
            when(testOrderRepository.countActive())
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> testOrderService.getTestOrderStatistics())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(testOrderRepository).countActive();
            verify(testOrderRepository, never()).countByStatus();
        }

        @Test
        @Order(9)
        @DisplayName("Should handle repository exception for countByStatus")
        void shouldHandleRepositoryExceptionForCountByStatus() {
            // Given
            when(testOrderRepository.countActive()).thenReturn(100L);
            when(testOrderRepository.countByStatus())
                    .thenThrow(new RuntimeException("Query execution failed"));

            // When & Then
            assertThatThrownBy(() -> testOrderService.getTestOrderStatistics())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Query execution failed");

            verify(testOrderRepository).countActive();
            verify(testOrderRepository).countByStatus();
        }

        @Test
        @Order(10)
        @DisplayName("Should handle unknown status in grouped counts gracefully")
        void shouldHandleUnknownStatusGracefully() {
            // Given
            long totalCount = 50L;
            List<Object[]> groupedCounts = Arrays.asList(
                    new Object[]{"PENDING", 20L},
                    new Object[]{"UNKNOWN_STATUS", 30L} // Unknown status - should be ignored
            );

            when(testOrderRepository.countActive()).thenReturn(totalCount);
            when(testOrderRepository.countByStatus()).thenReturn(groupedCounts);

            // When
            RestResponse<TestOrderStatisticResponse> response = testOrderService.getTestOrderStatistics();

            // Then - Unknown status should be ignored, not cause exception
            TestOrderStatisticResponse stats = response.getResult();
            assertThat(stats.getTotal()).isEqualTo(50L);
            assertThat(stats.getPending()).isEqualTo(20L);
            assertThat(stats.getCompleted()).isEqualTo(0L);
            assertThat(stats.getCancelled()).isEqualTo(0L);
            assertThat(stats.getReviewed()).isEqualTo(0L);
            assertThat(stats.getAiReviewed()).isEqualTo(0L);
        }

        @Test
        @Order(11)
        @DisplayName("Should handle null values in grouped counts array")
        void shouldHandleNullValuesInGroupedCountsArray() {
            // Given
            long totalCount = 10L;
            List<Object[]> groupedCounts = Collections.singletonList(
                    new Object[]{null, 10L} // null status
            );

            when(testOrderRepository.countActive()).thenReturn(totalCount);
            when(testOrderRepository.countByStatus()).thenReturn(groupedCounts);

            // When & Then - Should handle gracefully or throw NullPointerException
            assertThatThrownBy(() -> testOrderService.getTestOrderStatistics())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @Order(12)
        @DisplayName("Should handle mixed case status names")
        void shouldHandleMixedCaseStatusNames() {
            // Given
            long totalCount = 50L;
            List<Object[]> groupedCounts = Arrays.asList(
                    new Object[]{"pending", 20L}, // lowercase
                    new Object[]{"Completed", 30L} // mixed case
            );

            when(testOrderRepository.countActive()).thenReturn(totalCount);
            when(testOrderRepository.countByStatus()).thenReturn(groupedCounts);

            // When
            RestResponse<TestOrderStatisticResponse> response = testOrderService.getTestOrderStatistics();

            // Then - Case sensitive comparison, these won't match
            TestOrderStatisticResponse stats = response.getResult();
            assertThat(stats.getTotal()).isEqualTo(50L);
            assertThat(stats.getPending()).isEqualTo(0L); // Won't match "pending"
            assertThat(stats.getCompleted()).isEqualTo(0L); // Won't match "Completed"
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GET DAILY STATISTICS TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Daily Statistics Tests")
    class GetDailyStatisticsTests {

        @Test
        @Order(1)
        @DisplayName("Should return daily statistics with all days initialized to zero")
        void shouldReturnDailyStatisticsWithAllDaysInitialized() {
            // Given
            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Daily statistics retrieved successfully");
            assertThat(response.getResult()).isNotNull();
            assertThat(response.getResult().getDailyData()).hasSize(7);

            // Verify all days are initialized with zero counts
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            String[] expectedDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            for (int i = 0; i < 7; i++) {
                assertThat(dailyData.get(i).getDay()).isEqualTo(expectedDays[i]);
                assertThat(dailyData.get(i).getPending()).isEqualTo(0);
                assertThat(dailyData.get(i).getCompleted()).isEqualTo(0);
                assertThat(dailyData.get(i).getReviewed()).isEqualTo(0);
            }

            verify(testOrderRepository).findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @Order(2)
        @DisplayName("Should count pending orders created on specific days")
        void shouldCountPendingOrdersCreatedOnSpecificDays() {
            // Given
            LocalDateTime now = LocalDateTime.now();
            LocalDate today = now.toLocalDate();
            LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

            TestOrder pendingOrder1 = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.PENDING)
                    .build();
            pendingOrder1.setCreatedAt(monday.atStartOfDay()); // Monday

            TestOrder pendingOrder2 = TestOrder.builder()
                    .testOrderId("TO-002")
                    .status(TestOrderStatus.PENDING)
                    .build();
            pendingOrder2.setCreatedAt(monday.plusDays(1).atStartOfDay()); // Tuesday

            TestOrder pendingOrder3 = TestOrder.builder()
                    .testOrderId("TO-003")
                    .status(TestOrderStatus.PENDING)
                    .build();
            pendingOrder3.setCreatedAt(monday.atStartOfDay()); // Monday

            List<TestOrder> orders = Arrays.asList(pendingOrder1, pendingOrder2, pendingOrder3);

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(orders);

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            assertThat(dailyData.get(0).getDay()).isEqualTo("Mon");
            assertThat(dailyData.get(0).getPending()).isEqualTo(2); // Two pending orders on Monday
            assertThat(dailyData.get(1).getDay()).isEqualTo("Tue");
            assertThat(dailyData.get(1).getPending()).isEqualTo(1); // One pending order on Tuesday
        }

        @Test
        @Order(3)
        @DisplayName("Should count completed orders based on runAt time")
        void shouldCountCompletedOrdersBasedOnRunAt() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

            TestOrder completedOrder1 = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.COMPLETED)
                    .runAt(monday.plusDays(2).atStartOfDay()) // Wednesday
                    .build();
            completedOrder1.setCreatedAt(monday.atStartOfDay());

            TestOrder completedOrder2 = TestOrder.builder()
                    .testOrderId("TO-002")
                    .status(TestOrderStatus.COMPLETED)
                    .runAt(monday.plusDays(4).atStartOfDay()) // Friday
                    .build();
            completedOrder2.setCreatedAt(monday.atStartOfDay());

            List<TestOrder> orders = Arrays.asList(completedOrder1, completedOrder2);

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(orders);

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            assertThat(dailyData.get(2).getDay()).isEqualTo("Wed");
            assertThat(dailyData.get(2).getCompleted()).isEqualTo(1);
            assertThat(dailyData.get(4).getDay()).isEqualTo("Fri");
            assertThat(dailyData.get(4).getCompleted()).isEqualTo(1);
        }

        @Test
        @Order(4)
        @DisplayName("Should count reviewed orders based on reviewedAt time")
        void shouldCountReviewedOrdersBasedOnReviewedAt() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

            TestOrder reviewedOrder1 = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.REVIEWED)
                    .runAt(monday.plusDays(1).atStartOfDay())
                    .reviewedAt(monday.plusDays(3).atStartOfDay()) // Thursday
                    .build();
            reviewedOrder1.setCreatedAt(monday.atStartOfDay());

            TestOrder reviewedOrder2 = TestOrder.builder()
                    .testOrderId("TO-002")
                    .status(TestOrderStatus.REVIEWED)
                    .runAt(monday.plusDays(1).atStartOfDay())
                    .reviewedAt(monday.plusDays(5).atStartOfDay()) // Saturday
                    .build();
            reviewedOrder2.setCreatedAt(monday.atStartOfDay());

            List<TestOrder> orders = Arrays.asList(reviewedOrder1, reviewedOrder2);

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(orders);

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            assertThat(dailyData.get(3).getDay()).isEqualTo("Thu");
            assertThat(dailyData.get(3).getReviewed()).isEqualTo(1);
            assertThat(dailyData.get(5).getDay()).isEqualTo("Sat");
            assertThat(dailyData.get(5).getReviewed()).isEqualTo(1);
        }

        @Test
        @Order(5)
        @DisplayName("Should handle orders with multiple status transitions in same week")
        void shouldHandleOrdersWithMultipleStatusTransitions() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

            TestOrder order = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.REVIEWED)
                    .runAt(monday.plusDays(1).atStartOfDay()) // Tuesday - completed
                    .reviewedAt(monday.plusDays(2).atStartOfDay()) // Wednesday - reviewed
                    .build();
            order.setCreatedAt(monday.atStartOfDay()); // Monday - but status is REVIEWED, not PENDING

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(order));

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            assertThat(dailyData.get(0).getPending()).isEqualTo(0); // Monday - not pending
            assertThat(dailyData.get(1).getCompleted()).isEqualTo(1); // Tuesday - completed
            assertThat(dailyData.get(2).getReviewed()).isEqualTo(1); // Wednesday - reviewed
        }

        @Test
        @Order(6)
        @DisplayName("Should handle multiple orders on same day")
        void shouldHandleMultipleOrdersOnSameDay() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

            TestOrder order1 = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.PENDING)
                    .build();
            order1.setCreatedAt(monday.atStartOfDay());

            TestOrder order2 = TestOrder.builder()
                    .testOrderId("TO-002")
                    .status(TestOrderStatus.PENDING)
                    .build();
            order2.setCreatedAt(monday.atStartOfDay());

            TestOrder order3 = TestOrder.builder()
                    .testOrderId("TO-003")
                    .status(TestOrderStatus.COMPLETED)
                    .runAt(monday.atStartOfDay())
                    .build();
            order3.setCreatedAt(monday.atStartOfDay());

            TestOrder order4 = TestOrder.builder()
                    .testOrderId("TO-004")
                    .status(TestOrderStatus.REVIEWED)
                    .runAt(monday.atStartOfDay())
                    .reviewedAt(monday.atStartOfDay())
                    .build();
            order4.setCreatedAt(monday.atStartOfDay());

            List<TestOrder> orders = Arrays.asList(order1, order2, order3, order4);

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(orders);

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            assertThat(dailyData.get(0).getDay()).isEqualTo("Mon");
            assertThat(dailyData.get(0).getPending()).isEqualTo(2);
            assertThat(dailyData.get(0).getCompleted()).isEqualTo(2); // order3 and order4
            assertThat(dailyData.get(0).getReviewed()).isEqualTo(1); // order4
        }

        @Test
        @Order(7)
        @DisplayName("Should ignore orders with null createdAt for pending count")
        void shouldIgnoreOrdersWithNullCreatedAtForPendingCount() {
            // Given
            TestOrder order = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.PENDING)
                    .build();
            order.setCreatedAt(null); // null createdAt

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(order));

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            // All days should have 0 pending
            for (DailyStatisticsResponse.DailyData data : dailyData) {
                assertThat(data.getPending()).isEqualTo(0);
            }
        }

        @Test
        @Order(8)
        @DisplayName("Should ignore orders with null runAt for completed count")
        void shouldIgnoreOrdersWithNullRunAtForCompletedCount() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

            TestOrder order = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.COMPLETED)
                    .runAt(null) // null runAt
                    .build();
            order.setCreatedAt(monday.atStartOfDay());

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(order));

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            // All days should have 0 completed
            for (DailyStatisticsResponse.DailyData data : dailyData) {
                assertThat(data.getCompleted()).isEqualTo(0);
            }
        }

        @Test
        @Order(9)
        @DisplayName("Should ignore orders with null reviewedAt for reviewed count")
        void shouldIgnoreOrdersWithNullReviewedAtForReviewedCount() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

            TestOrder order = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.REVIEWED)
                    .runAt(monday.atStartOfDay())
                    .reviewedAt(null) // null reviewedAt
                    .build();
            order.setCreatedAt(monday.atStartOfDay());

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.singletonList(order));

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            // All days should have 0 reviewed
            for (DailyStatisticsResponse.DailyData data : dailyData) {
                assertThat(data.getReviewed()).isEqualTo(0);
            }
        }

        @Test
        @Order(10)
        @DisplayName("Should only count orders with PENDING status for pending count")
        void shouldOnlyCountPendingStatusForPendingCount() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

            TestOrder pendingOrder = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.PENDING)
                    .build();
            pendingOrder.setCreatedAt(monday.atStartOfDay());

            TestOrder completedOrder = TestOrder.builder()
                    .testOrderId("TO-002")
                    .status(TestOrderStatus.COMPLETED)
                    .build();
            completedOrder.setCreatedAt(monday.atStartOfDay()); // Same day but COMPLETED

            TestOrder reviewedOrder = TestOrder.builder()
                    .testOrderId("TO-003")
                    .status(TestOrderStatus.REVIEWED)
                    .build();
            reviewedOrder.setCreatedAt(monday.atStartOfDay()); // Same day but REVIEWED

            List<TestOrder> orders = Arrays.asList(pendingOrder, completedOrder, reviewedOrder);

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(orders);

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            assertThat(dailyData.get(0).getDay()).isEqualTo("Mon");
            assertThat(dailyData.get(0).getPending()).isEqualTo(1); // Only the PENDING order
        }

        @Test
        @Order(11)
        @DisplayName("Should ignore runAt outside current week for completed count")
        void shouldIgnoreRunAtOutsideCurrentWeekForCompletedCount() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

            TestOrder orderInWeek = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.COMPLETED)
                    .runAt(monday.plusDays(1).atStartOfDay()) // Tuesday - in week
                    .build();
            orderInWeek.setCreatedAt(monday.atStartOfDay());

            TestOrder orderOutsideWeek = TestOrder.builder()
                    .testOrderId("TO-002")
                    .status(TestOrderStatus.COMPLETED)
                    .runAt(monday.minusDays(7).atStartOfDay()) // Last week
                    .build();
            orderOutsideWeek.setCreatedAt(monday.atStartOfDay());

            List<TestOrder> orders = Arrays.asList(orderInWeek, orderOutsideWeek);

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(orders);

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            assertThat(dailyData.get(1).getDay()).isEqualTo("Tue");
            assertThat(dailyData.get(1).getCompleted()).isEqualTo(1); // Only orderInWeek
        }

        @Test
        @Order(12)
        @DisplayName("Should ignore reviewedAt outside current week for reviewed count")
        void shouldIgnoreReviewedAtOutsideCurrentWeekForReviewedCount() {
            // Given
            LocalDate today = LocalDate.now();
            LocalDate monday = today.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY));

            TestOrder orderInWeek = TestOrder.builder()
                    .testOrderId("TO-001")
                    .status(TestOrderStatus.REVIEWED)
                    .runAt(monday.atStartOfDay())
                    .reviewedAt(monday.plusDays(2).atStartOfDay()) // Wednesday - in week
                    .build();

            TestOrder orderOutsideWeek = TestOrder.builder()
                    .testOrderId("TO-002")
                    .status(TestOrderStatus.REVIEWED)
                    .runAt(monday.atStartOfDay())
                    .reviewedAt(monday.plusDays(10).atStartOfDay()) // Next week
                    .build();

            List<TestOrder> orders = Arrays.asList(orderInWeek, orderOutsideWeek);

            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(orders);

            // When
            RestResponse<DailyStatisticsResponse> response = testOrderService.getDailyStatistics();

            // Then
            List<DailyStatisticsResponse.DailyData> dailyData = response.getResult().getDailyData();
            assertThat(dailyData.get(2).getDay()).isEqualTo("Wed");
            assertThat(dailyData.get(2).getReviewed()).isEqualTo(1); // Only orderInWeek
        }

        @Test
        @Order(13)
        @DisplayName("Should handle repository returning null list gracefully")
        void shouldHandleRepositoryReturningNull() {
            // Given
            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> testOrderService.getDailyStatistics())
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @Order(14)
        @DisplayName("Should handle repository exception")
        void shouldHandleRepositoryException() {
            // Given
            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenThrow(new RuntimeException("Database connection error"));

            // When & Then
            assertThatThrownBy(() -> testOrderService.getDailyStatistics())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database connection error");

            verify(testOrderRepository).findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class));
        }

        @Test
        @Order(15)
        @DisplayName("Should return correct week range from Monday to Sunday")
        void shouldReturnCorrectWeekRangeFromMondayToSunday() {
            // Given
            when(testOrderRepository.findTestOrdersInCurrentWeek(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .thenReturn(Collections.emptyList());

            // When
            testOrderService.getDailyStatistics();

            // Then - Verify repository is called with correct date range
            verify(testOrderRepository).findTestOrdersInCurrentWeek(
                    argThat(startDateTime -> startDateTime.getDayOfWeek() == java.time.DayOfWeek.MONDAY),
                    argThat(endDateTime -> {
                        // endDateTime should be next Monday (Sunday + 1 day)
                        LocalDateTime start = LocalDate.now()
                                .with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                                .atStartOfDay();
                        LocalDateTime expectedEnd = start.plusDays(7);
                        return endDateTime.equals(expectedEnd);
                    })
            );
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // REVIEW TEST ORDER TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Review Test Order Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReviewTestOrderTests {

        @Test
        @Order(1)
        @DisplayName("Should review test order successfully when status is COMPLETED")
        void shouldReviewTestOrderSuccessfully() {
            // Given
            String orderId = "TO-001";
            testOrder.setStatus(TestOrderStatus.COMPLETED); // Chỉ set 1 lần

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class)))
                    .thenAnswer(invocation -> {
                        TestOrder savedOrder = invocation.getArgument(0);
                        return TestOrderResponse.builder()
                                .testOrderId(savedOrder.getTestOrderId())
                                .status(savedOrder.getStatus())
                                .build();
                    });

            // When
            RestResponse<TestOrderResponse> response = testOrderService.reviewTestOrder(orderId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage()).isEqualTo("Test order reviewed successfully");
            assertThat(response.getResult().getStatus()).isEqualTo(TestOrderStatus.REVIEWED);

            // Verify các interaction
            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository).save(argThat(order ->
                    order.getStatus() == TestOrderStatus.REVIEWED &&
                            order.getReviewedAt() != null &&
                            "System".equals(order.getReviewedBy())
            ));

            // QUAN TRỌNG: Verify event publisher được gọi
            verify(eventPublisher).publishStatusChanged(
                    eq(orderId),
                    eq(TestOrderStatus.COMPLETED),
                    eq(TestOrderStatus.REVIEWED)
            );
        }

        @Test
        @Order(2)
        @DisplayName("Should throw IllegalStateException when status is not COMPLETED")
        void shouldThrowIllegalStateExceptionWhenStatusIsNotCompleted() {
            // Given
            String orderId = "TO-001";
            testOrder.setStatus(TestOrderStatus.PENDING); // Invalid initial state

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));

            // When & Then
            assertThatThrownBy(() -> testOrderService.reviewTestOrder(orderId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Only COMPLETED test orders can be reviewed. Current status: PENDING");

            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository, never()).save(any());
            verify(eventPublisher, never()).publishStatusChanged(anyString(), any(), any());
        }

        @Test
        @Order(3)
        @DisplayName("Should throw ResourceNotFoundException when order not found")
        void shouldThrowResourceNotFoundExceptionWhenOrderNotFound() {
            // Given
            String orderId = "NON-EXISTENT-ID";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testOrderService.reviewTestOrder(orderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository, never()).save(any());
        }

        @Test
        @Order(4)
        @DisplayName("Should not publish event if publisher is null")
        void shouldNotPublishEventIfPublisherIsNull() {
            // Given
            // Use reflection or a setter to make the publisher null for this specific test
            // For simplicity, we'll assume a new instance of the service without the mocked publisher
            TestOrderServiceImpl serviceWithNullPublisher = new TestOrderServiceImpl(
                    testOrderRepository, testOrderMapper, commentMapper, instrumentSyncService
            );

            String orderId = "TO-001";
            testOrder.setStatus(TestOrderStatus.COMPLETED);

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            serviceWithNullPublisher.reviewTestOrder(orderId);

            // Then
            // No verification for eventPublisher because it's not mocked in this instance
            verify(testOrderRepository).save(any());
        }
    }
}

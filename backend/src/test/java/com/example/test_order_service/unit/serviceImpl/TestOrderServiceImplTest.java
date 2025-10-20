package com.example.test_order_service.unit.serviceImpl;

import com.example.test_order_service.dto.repsonse.PageResponse;
import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.ResultStatus;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.mapper.TestOrderMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.serviceImpl.TestOrderServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
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

    @InjectMocks
    private TestOrderServiceImpl testOrderService;

    private TestOrderRequest testOrderRequest;
    private TestOrderUpdateRequest updateRequest;
    private TestOrder testOrder;
    private TestOrderResponse testOrderResponse;

    @BeforeEach
    void setUp() {
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
                .status(ResultStatus.PENDING)
                .createdBy("System")
                .build();

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
                .status(ResultStatus.PENDING)
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

            verify(testOrderMapper).toTestOrderEntity(testOrderRequest);
            verify(testOrderRepository).save(any(TestOrder.class));
            verify(testOrderMapper).toTestOrderResponse(testOrder);
        }

        @Test
        @Order(2)
        @DisplayName("Should set createdBy to System")
        void shouldSetCreatedByToSystem() {
            // Given
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
        @DisplayName("Should handle repository exception during create")
        void shouldHandleRepositoryExceptionDuringCreate() {
            // Given
            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> testOrderService.createTestOrder(testOrderRequest))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");

            verify(testOrderRepository).save(any(TestOrder.class));
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
        @Order(4)
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
        @Order(5)
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
        @Order(6)
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
        @Order(7)
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
                            originalCitizenId.equals(order.getCitizenId())
            ));
        }

        @Test
        @Order(8)
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
        @Order(9)
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
    }

    // ═══════════════════════════════════════════════════════════════
    // GET TEST ORDERS TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Get Test Orders Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetTestOrdersTests {

        @Test
        @Order(10)
        @DisplayName("Should return paginated test orders")
        void shouldReturnPaginatedTestOrders() {
            // Given
            TestOrder order1 = TestOrder.builder()
                    .testOrderId("TO-001")
                    .patientName("Patient 1")
                    .build();

            TestOrder order2 = TestOrder.builder()
                    .testOrderId("TO-002")
                    .patientName("Patient 2")
                    .build();

            Page<TestOrder> testOrderPage = new PageImpl<>(
                    Arrays.asList(order1, order2),
                    PageRequest.of(0, 6),
                    2
            );

            TestOrderResponse response1 = TestOrderResponse.builder()
                    .testOrderId("TO-001")
                    .patientName("Patient 1")
                    .build();

            TestOrderResponse response2 = TestOrderResponse.builder()
                    .testOrderId("TO-002")
                    .patientName("Patient 2")
                    .build();

            when(testOrderRepository.findTestOrdersByParams(any(Pageable.class), anyString()))
                    .thenReturn(testOrderPage);
            when(testOrderMapper.toTestOrderResponse(order1)).thenReturn(response1);
            when(testOrderMapper.toTestOrderResponse(order2)).thenReturn(response2);

            // When
            Pageable pageable = PageRequest.of(0, 6);
            PageResponse<TestOrderResponse> response = testOrderService.getTestOrders(pageable, "");

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getCurrentPage()).isEqualTo(1); // Page number starts from 1
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getItems()).hasSize(2);
            assertThat(response.getItems().get(0).getTestOrderId()).isEqualTo("TO-001");
            assertThat(response.getItems().get(1).getTestOrderId()).isEqualTo("TO-002");

            verify(testOrderRepository).findTestOrdersByParams(pageable, "");
            verify(testOrderMapper, times(2)).toTestOrderResponse(any(TestOrder.class));
        }

        @Test
        @Order(11)
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
        @Order(12)
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
        @Order(13)
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
            assertThat(response.getCurrentPage()).isEqualTo(2); // Should be page 2
            assertThat(response.getTotalPages()).isEqualTo(3);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE TEST ORDER TESTS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Delete Test Order Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteTestOrderTests {

        @Test
        @Order(14)
        @DisplayName("Should delete test order successfully")
        void shouldDeleteTestOrderSuccessfully() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            doNothing().when(testOrderRepository).deleteById(orderId);

            // When
            RestResponse<Void> response = testOrderService.deleteTestOrder(orderId);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(200);
            assertThat(response.getMessage().toString()).contains("deleted successfully");
            assertThat(response.getMessage().toString()).contains(orderId);
            assertThat(response.getTimestamp()).isNotNull();
            assertThat(response.getResult()).isNull();

            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository).deleteById(orderId);
        }

        @Test
        @Order(15)
        @DisplayName("Should throw ResourceNotFoundException when test order not found for deletion")
        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
            // Given
            String invalidOrderId = "INVALID-ID";
            when(testOrderRepository.findById(invalidOrderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testOrderService.deleteTestOrder(invalidOrderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(invalidOrderId);
            verify(testOrderRepository, never()).deleteById(anyString());
        }

        @Test
        @Order(16)
        @DisplayName("Should handle repository exception during deletion")
        void shouldHandleRepositoryExceptionDuringDeletion() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            doThrow(new RuntimeException("Database error"))
                    .when(testOrderRepository).deleteById(orderId);

            // When & Then
            assertThatThrownBy(() -> testOrderService.deleteTestOrder(orderId))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("Database error");

            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository).deleteById(orderId);
        }
    }
}
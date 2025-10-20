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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    //Setup common test data
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

        updateRequest = new TestOrderUpdateRequest();
        updateRequest.setPatientName("Nguyen Van B");
        updateRequest.setDateOfBirth(LocalDate.of(1992, 5, 20));
        updateRequest.setGender(Gender.FEMALE);
        updateRequest.setPhone("0909876543");
        updateRequest.setAddress("456 Le Loi, HCMC");
        updateRequest.setEmail("nguyenvanb@example.com");

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
                .age(35)
                .gender(Gender.MALE)
                .address("123 Nguyen Hue, HCMC")
                .email("nguyenvana@example.com")
                .phone("0901234567")
                .status(ResultStatus.PENDING)
                .build();
    }

    //This is the test for createTestOrder method
    @Nested
    @DisplayName("Create Test Order Tests")
    class CreateTestOrderTests {

        @Test
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
            assertThat(response.getResult().getAge()).isEqualTo(35);

            verify(testOrderMapper).toTestOrderEntity(testOrderRequest);
            verify(testOrderRepository).save(any(TestOrder.class));
            verify(testOrderMapper).toTestOrderResponse(testOrder);
        }

        @Test
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
        @DisplayName("Should calculate age correctly")
        void shouldCalculateAgeCorrectly() {
            // Given
            TestOrderRequest request = TestOrderRequest.builder()
                    .patientName("Test Patient")
                    .dateOfBirth(LocalDate.of(2000, 1, 1))
                    .citizenId("001234567890")
                    .country("Vietnam")
                    .gender(Gender.MALE)
                    .phone("0901234567")
                    .build();

            when(testOrderMapper.toTestOrderEntity(any(TestOrderRequest.class))).thenReturn(testOrder);
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            RestResponse<TestOrderResponse> response = testOrderService.createTestOrder(request);

            // Then
            int expectedAge = LocalDate.now().getYear() - 2000;
            assertThat(response.getResult().getAge()).isEqualTo(expectedAge);
        }
    }

    @Nested
    @DisplayName("Update Test Order Tests")
    class UpdateTestOrderTests {

        @Test
        @DisplayName("Should update test order successfully")
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

            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository).save(any(TestOrder.class));
            verify(testOrderMapper).toTestOrderResponse(any(TestOrder.class));
        }

        @Test
        @DisplayName("Should update only provided fields")
        void shouldUpdateOnlyProvidedFields() {
            // Given
            String orderId = "TO-001";
            TestOrderUpdateRequest partialUpdate = new TestOrderUpdateRequest();
            partialUpdate.setPatientName("New Name");
            // Other fields are null

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            testOrderService.updateTestOrder(orderId, partialUpdate);

            // Then
            verify(testOrderRepository).save(argThat(order ->
                    "New Name".equals(order.getPatientName()) &&
                            testOrder.getGender().equals(order.getGender()) &&
                            testOrder.getPhone().equals(order.getPhone())
            ));
        }

        @Test
        @DisplayName("Should return 404 when test order not found")
        void shouldReturn404WhenTestOrderNotFound() {
            // Given
            String orderId = "INVALID-ID";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.empty());

            // When
            RestResponse<TestOrderResponse> response = testOrderService.updateTestOrder(orderId, updateRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(404);
            assertThat(response.getMessage()).isEqualTo("Test order not found");
            assertThat(response.getResult()).isNull();

            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository, never()).save(any(TestOrder.class));
        }

        @Test
        @DisplayName("Should return 500 on unexpected error")
        void shouldReturn500OnUnexpectedError() {
            // Given
            String orderId = "TO-001";
            when(testOrderRepository.findById(orderId)).thenThrow(new RuntimeException("Database error"));

            // When
            RestResponse<TestOrderResponse> response = testOrderService.updateTestOrder(orderId, updateRequest);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(500);
            assertThat(response.getMessage()).isEqualTo("An error occurred while updating the test order");
            assertThat(response.getResult()).isNull();
        }

        @Test
        @DisplayName("Should recalculate age after update")
        void shouldRecalculateAgeAfterUpdate() {
            // Given
            String orderId = "TO-001";
            TestOrderUpdateRequest updateWithNewDob = new TestOrderUpdateRequest();
            updateWithNewDob.setDateOfBirth(LocalDate.of(1995, 6, 15));

            when(testOrderRepository.findById(orderId)).thenReturn(Optional.of(testOrder));
            when(testOrderRepository.save(any(TestOrder.class))).thenReturn(testOrder);
            when(testOrderMapper.toTestOrderResponse(any(TestOrder.class))).thenReturn(testOrderResponse);

            // When
            RestResponse<TestOrderResponse> response = testOrderService.updateTestOrder(orderId, updateWithNewDob);

            // Then
            int expectedAge = LocalDate.now().getYear() - 1995;
            assertThat(response.getResult().getAge()).isEqualTo(expectedAge);
        }
    }

    //This is the test for getTestOrders method
    @Nested
    @DisplayName("Get Test Orders Tests")
    class GetTestOrdersTests {

        @Test
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
            assertThat(response.getCurrentPage()).isEqualTo(1);
            assertThat(response.getTotalPages()).isEqualTo(1);
            assertThat(response.getItems()).hasSize(2);
            assertThat(response.getItems().get(0).getTestOrderId()).isEqualTo("TO-001");
            assertThat(response.getItems().get(1).getTestOrderId()).isEqualTo("TO-002");

            verify(testOrderRepository).findTestOrdersByParams(pageable, "");
            verify(testOrderMapper, times(2)).toTestOrderResponse(any(TestOrder.class));
        }

        @Test
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

            verify(testOrderRepository).findTestOrdersByParams(pageable, keyword);
        }

        @Test
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
    }

    @Nested
    @DisplayName("Delete Test Order Tests")
    class DeleteTestOrderTests {

        @Test
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

            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository).deleteById(orderId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when test order not found")
        void shouldThrowResourceNotFoundExceptionWhenTestOrderNotFound() {
            // Given
            String orderId = "INVALID-ID";
            when(testOrderRepository.findById(orderId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> testOrderService.deleteTestOrder(orderId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Test order not found");

            verify(testOrderRepository).findById(orderId);
            verify(testOrderRepository, never()).deleteById(anyString());
        }
    }
}

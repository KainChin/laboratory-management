package com.example.test_order_service.integration.controller;

import com.example.test_order_service.dto.repsonse.PageResponse;
import com.example.test_order_service.dto.repsonse.RestResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.request.TestOrderUpdateRequest;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.service.TestOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Complete Integration Tests for TestOrderController
 * Covers all endpoints and edge cases
 */
@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@DisplayName("TestOrderController Integration Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TestOrderService testOrderService;

    private TestOrderRequest testOrderRequest;
    private TestOrderResponse testOrderResponse;
    private RestResponse<TestOrderResponse> restResponse;
    private ObjectMapper objectMapper;

    @BeforeEach
    void initData() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        testOrderRequest = TestOrderRequest.builder()
                .patientName("John Doe")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .citizenId("079399004953")
                .country("Vietnam")
                .gender(Gender.MALE)
                .address("123 Main St, Hanoi")
                .email("johndoe@gmail.com")
                .phone("+84123456789")
                .build();

        testOrderResponse = TestOrderResponse.builder()
                .testOrderId("6b1f2e77-29d3-4e58-8a32-b9e7c3c3b1f0")
                .patientName("John Doe")
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .citizenId("079399004953")
                .country("Vietnam")
                .gender(Gender.MALE)
                .address("123 Main St, Hanoi")
                .email("johndoe@gmail.com")
                .phone("+84123456789")
                .createdBy("System")
                .status(TestOrderStatus.PENDING)
                .build();

        restResponse = RestResponse.<TestOrderResponse>builder()
                .statusCode(200)
                .result(testOrderResponse)
                .message("Test order created successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // POST /api/test-orders - CREATE TEST ORDER
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("POST /api/test-orders - Create Test Order")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateTestOrderTests {

        @Test
        @Order(1)
        @DisplayName("Should create test order with valid request")
        public void createTestOrder_validRequest_success() throws Exception {
            //Given
            String validContent = objectMapper.writeValueAsString(testOrderRequest);
            when(testOrderService.createTestOrder(ArgumentMatchers.any(TestOrderRequest.class)))
                    .thenReturn(restResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(validContent))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Test order created successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.testOrderId").value("6b1f2e77-29d3-4e58-8a32-b9e7c3c3b1f0"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.patientName").value("John Doe"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.gender").value("MALE"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.status").value("PENDING"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.createdBy").value("System"));
        }

        @Test
        @Order(2)
        @DisplayName("Should fail when missing all required fields")
        public void createTestOrder_missingRequiredFields_fail() throws Exception {
            //Given
            testOrderRequest.setPatientName("");
            testOrderRequest.setCitizenId("");
            testOrderRequest.setCountry("");
            testOrderRequest.setPhone("");
            String invalidContent = objectMapper.writeValueAsString(testOrderRequest);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(invalidContent))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(400))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Bad Request"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.timestamp").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Patient name is required.*/)]").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Citizen ID is required.*/)]").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Country is required.*/)]").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Phone number is required.*/)]").exists());
        }

        @Test
        @Order(3)
        @DisplayName("Should fail with invalid date format (yyyy-MM-dd)")
        public void createTestOrder_invalidDateFormat_fail() throws Exception {
            //Given
            String invalidJsonContent = """
                    {
                        "patientName": "John Doe",
                        "dateOfBirth": "1980-01-01",
                        "citizenId": "079399004953",
                        "country": "Vietnam",
                        "gender": "MALE",
                        "address": "123 Main St, Hanoi",
                        "email": "johndoe@gmail.com",
                        "phone": "+84123456789"
                    }
                    """;

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(invalidJsonContent))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(400))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Bad Request"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("Birthdate must in dd/MM/yyyy format"));
        }

        @Test
        @Order(4)
        @DisplayName("Should fail with invalid gender enum value")
        public void createTestOrder_invalidGender_fail() throws Exception {
            //Given
            String invalidJsonContent = """
                    {
                        "patientName": "John Doe",
                        "dateOfBirth": "01/01/1980",
                        "citizenId": "079399004953",
                        "country": "Vietnam",
                        "gender": "UNKNOWN",
                        "address": "123 Main St, Hanoi",
                        "email": "johndoe@gmail.com",
                        "phone": "+84123456789"
                    }
                    """;

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(invalidJsonContent))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(400))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("Gender must be one of: MALE, FEMALE, OTHER"));
        }

        @Test
        @Order(5)
        @DisplayName("Should fail with invalid email and phone format")
        public void createTestOrder_invalidEmailAndPhone_fail() throws Exception {
            //Given
            testOrderRequest.setEmail("invalid-email");
            testOrderRequest.setPhone("invalid-phone");
            String invalidContent = objectMapper.writeValueAsString(testOrderRequest);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(invalidContent))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(400))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Email should be valid.*/)]").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Phone number is invalid.*/)]").exists());
        }

        @Test
        @Order(6)
        @DisplayName("Should fail with only missing patient name")
        public void createTestOrder_missingPatientName_fail() throws Exception {
            //Given
            testOrderRequest.setPatientName("");
            String invalidContent = objectMapper.writeValueAsString(testOrderRequest);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(invalidContent))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Patient name is required.*/)]").exists());
        }

        @Test
        @Order(7)
        @DisplayName("Should fail with malformed JSON")
        public void createTestOrder_malformedJson_fail() throws Exception {
            //Given
            String malformedJson = "{ invalid json }";

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(malformedJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @Order(8)
        @DisplayName("Should create test order with optional fields as null")
        public void createTestOrder_withOptionalFieldsNull_success() throws Exception {
            //Given
            testOrderRequest.setAddress(null);
            testOrderRequest.setEmail(null);
            String validContent = objectMapper.writeValueAsString(testOrderRequest);
            when(testOrderService.createTestOrder(any(TestOrderRequest.class)))
                    .thenReturn(restResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(validContent))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // PUT /api/test-orders/{orderId} - UPDATE TEST ORDER
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PUT /api/test-orders/{orderId} - Update Test Order")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class UpdateTestOrderTests {

        @Test
        @Order(9)
        @DisplayName("Should update test order with valid request")
        public void updateTestOrder_validRequest_success() throws Exception {
            //Given
            String orderId = "6b1f2e77-29d3-4e58-8a32-b9e7c3c3b1f0";
            TestOrderUpdateRequest updateRequest = TestOrderUpdateRequest.builder()
                    .patientName("Jane Doe")
                    .dateOfBirth(LocalDate.of(1985, 5, 15))
                    .gender(Gender.FEMALE)
                    .phone("+84987654321")
                    .address("456 Oak St, HCMC")
                    .email("janedoe@gmail.com")
                    .build();

            TestOrderResponse updatedResponse = TestOrderResponse.builder()
                    .testOrderId(orderId)
                    .patientName("Jane Doe")
                    .dateOfBirth(LocalDate.of(1985, 5, 15))
                    .gender(Gender.FEMALE)
                    .phone("+84987654321")
                    .address("456 Oak St, HCMC")
                    .email("janedoe@gmail.com")
                    .status(TestOrderStatus.PENDING)
                    .build();

            RestResponse<TestOrderResponse> updateRestResponse = RestResponse.<TestOrderResponse>builder()
                    .statusCode(200)
                    .result(updatedResponse)
                    .message("Test order updated successfully")
                    .timestamp(LocalDateTime.now())
                    .build();

            String requestContent = objectMapper.writeValueAsString(updateRequest);
            when(testOrderService.updateTestOrder(eq(orderId), any(TestOrderUpdateRequest.class)))
                    .thenReturn(updateRestResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .put("/api/test-orders/" + orderId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(requestContent))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Test order updated successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.patientName").value("Jane Doe"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.phone").value("+84987654321"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.email").value("janedoe@gmail.com"));
        }

        @Test
        @Order(10)
        @DisplayName("Should update test order with partial data")
        public void updateTestOrder_partialUpdate_success() throws Exception {
            //Given
            String orderId = "6b1f2e77-29d3-4e58-8a32-b9e7c3c3b1f0";
            TestOrderUpdateRequest updateRequest = new TestOrderUpdateRequest();
            updateRequest.setPatientName("Updated Name");
            updateRequest.setPhone("+84999999999");

            RestResponse<TestOrderResponse> updateRestResponse = RestResponse.<TestOrderResponse>builder()
                    .statusCode(200)
                    .result(testOrderResponse)
                    .message("Test order updated successfully")
                    .timestamp(LocalDateTime.now())
                    .build();

            String requestContent = objectMapper.writeValueAsString(updateRequest);
            when(testOrderService.updateTestOrder(eq(orderId), any(TestOrderUpdateRequest.class)))
                    .thenReturn(updateRestResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .put("/api/test-orders/" + orderId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(requestContent))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
        }

        @Test
        @Order(11)
        @DisplayName("Should return 404 when order not found")
        public void updateTestOrder_orderNotFound_fail() throws Exception {
            //Given
            String nonExistentId = "non-existent-id";
            TestOrderUpdateRequest updateRequest = new TestOrderUpdateRequest();
            updateRequest.setPatientName("Updated Name");

            RestResponse<TestOrderResponse> notFoundResponse = RestResponse.<TestOrderResponse>builder()
                    .statusCode(404)
                    .message("Test order not found")
                    .timestamp(LocalDateTime.now())
                    .build();

            String requestContent = objectMapper.writeValueAsString(updateRequest);
            when(testOrderService.updateTestOrder(eq(nonExistentId), any(TestOrderUpdateRequest.class)))
                    .thenReturn(notFoundResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .put("/api/test-orders/" + nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(requestContent))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Test order not found"));
        }

        @Test
        @Order(12)
        @DisplayName("Should fail with invalid email in update request")
        public void updateTestOrder_invalidEmail_fail() throws Exception {
            //Given
            String orderId = "6b1f2e77-29d3-4e58-8a32-b9e7c3c3b1f0";
            TestOrderUpdateRequest updateRequest = new TestOrderUpdateRequest();
            updateRequest.setEmail("invalid-email-format");

            String requestContent = objectMapper.writeValueAsString(updateRequest);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .put("/api/test-orders/" + orderId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(requestContent))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Email should be valid.*/)]").exists());
        }

        @Test
        @Order(13)
        @DisplayName("Should fail with invalid date format in update")
        public void updateTestOrder_invalidDateFormat_fail() throws Exception {
            //Given
            String orderId = "6b1f2e77-29d3-4e58-8a32-b9e7c3c3b1f0";
            String invalidJsonContent = """
                    {
                        "patientName": "Jane Doe",
                        "dateOfBirth": "1985-05-15",
                        "phone": "+84987654321"
                    }
                    """;

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .put("/api/test-orders/" + orderId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(invalidJsonContent))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("Birthdate must in dd/MM/yyyy format"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // GET /api/test-orders - GET ALL TEST ORDERS WITH PAGINATION
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("GET /api/test-orders - Get All Test Orders")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class GetTestOrdersTests {

        @Test
        @Order(13)
        @DisplayName("Should get all test orders with default parameters")
        public void getTestOrders_defaultParameters_success() throws Exception {
            //Given
            List<TestOrderResponse> testOrders = Arrays.asList(
                    testOrderResponse,
                    TestOrderResponse.builder()
                            .testOrderId("another-id")
                            .patientName("Jane Smith")
                            .status(TestOrderStatus.COMPLETED)
                            .build()
            );

            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(testOrders)
                    .build();

            RestResponse<PageResponse<TestOrderResponse>> getRestResponse = RestResponse.<PageResponse<TestOrderResponse>>builder()
                    .statusCode(200)
                    .message("Test orders retrieved successfully")
                    .result(pageResponse)
                    .timestamp(LocalDateTime.now())
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Test orders retrieved successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.currentPage").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.totalPages").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.items").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.items.length()").value(2));
        }

        @Test
        @Order(14)
        @DisplayName("Should get test orders with keyword search")
        public void getTestOrders_withKeyword_success() throws Exception {
            //Given
            String keyword = "John";
            List<TestOrderResponse> filteredOrders = Collections.singletonList(testOrderResponse);

            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(filteredOrders)
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), eq(keyword)))
                    .thenReturn(pageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("keyword", keyword)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.items.length()").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.items[0].patientName").value("John Doe"));
        }

        @Test
        @Order(15)
        @DisplayName("Should get test orders with custom page and size")
        public void getTestOrders_withCustomPageSize_success() throws Exception {
            //Given
            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(2)
                    .totalPages(3)
                    .items(Collections.singletonList(testOrderResponse))
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("page", "2")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.currentPage").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.totalPages").value(3));
        }

        @Test
        @Order(16)
        @DisplayName("Should get test orders sorted ascending")
        public void getTestOrders_sortedAscending_success() throws Exception {
            //Given
            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(Collections.singletonList(testOrderResponse))
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("sortBy", "patientName")
                            .param("sortDir", "asc")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
        }

        @Test
        @Order(17)
        @DisplayName("Should get test orders sorted descending")
        public void getTestOrders_sortedDescending_success() throws Exception {
            //Given
            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(Collections.singletonList(testOrderResponse))
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("sortBy", "createdAt")
                            .param("sortDir", "desc")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
        }

        @Test
        @Order(18)
        @DisplayName("Should return empty list when no orders found")
        public void getTestOrders_noOrdersFound_returnEmptyList() throws Exception {
            //Given
            PageResponse<TestOrderResponse> emptyPageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(0)
                    .items(Collections.emptyList())
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), anyString()))
                    .thenReturn(emptyPageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.items").isEmpty())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.totalPages").value(0));
        }

        @Test
        @Order(19)
        @DisplayName("Should handle page number less than 1")
        public void getTestOrders_pageNumberLessThanOne_usePageZero() throws Exception {
            //Given
            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(Collections.singletonList(testOrderResponse))
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("page", "0")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200));
        }

        @Test
        @Order(20)
        @DisplayName("Should sort by different fields")
        public void getTestOrders_sortByDifferentFields_success() throws Exception {
            //Given
            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(Collections.singletonList(testOrderResponse))
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            //When & Then - Sort by email
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("sortBy", "email")
                            .param("sortDir", "asc")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            //When & Then - Sort by phone
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("sortBy", "phone")
                            .param("sortDir", "desc")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @Order(21)
        @DisplayName("Should handle all parameters together")
        public void getTestOrders_allParameters_success() throws Exception {
            //Given
            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(2)
                    .totalPages(5)
                    .items(Collections.singletonList(testOrderResponse))
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), eq("John")))
                    .thenReturn(pageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("keyword", "John")
                            .param("page", "2")
                            .param("size", "10")
                            .param("sortBy", "patientName")
                            .param("sortDir", "asc")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.currentPage").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.totalPages").value(5));
        }

        @Test
        @Order(22)
        @DisplayName("Should handle case-insensitive sort direction")
        public void getTestOrders_caseInsensitiveSortDir_success() throws Exception {
            //Given
            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(Collections.singletonList(testOrderResponse))
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            //When & Then - Test with uppercase
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("sortDir", "ASC")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            //When & Then - Test with lowercase
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("sortDir", "desc")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk());

            //When & Then - Test with mixed case
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("sortDir", "DeSc")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE /api/test-orders/{orderId} - SOFT DELETE TEST ORDER
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("DELETE /api/test-orders/{orderId} - Soft Delete Test Order")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class DeleteTestOrderTests {

        @Test
        @Order(23)
        @DisplayName("Should soft delete test order successfully and return updated page")
        public void deleteTestOrder_validOrderId_success() throws Exception {
            // Given
            String orderId = "6b1f2e77-29d3-4e58-8a32-b9e7c3c3b1f0";

            TestOrderResponse testOrderResponse = TestOrderResponse.builder()
                    .testOrderId(orderId)
                    .patientName("Nguyen Van A")
                    .country("Vietnam")
                    .status(TestOrderStatus.PENDING)
                    .build();

            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(List.of(testOrderResponse))
                    .build();

            when(testOrderService.deleteTestOrder(eq(orderId), any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/test-orders/{orderId}?page=1&size=6&keyword=", orderId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Test orders retrieved successfully"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.items[0].testOrderId").value(orderId))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.currentPage").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.totalPages").value(1));

            verify(testOrderService).deleteTestOrder(eq(orderId), any(Pageable.class), anyString());
        }

        @Test
        @Order(24)
        @DisplayName("Should return 404 when deleting non-existent test order")
        public void deleteTestOrder_orderNotFound_fail() throws Exception {
            // Given
            String nonExistentId = "non-existent-id";
            when(testOrderService.deleteTestOrder(eq(nonExistentId), any(Pageable.class), anyString()))
                    .thenThrow(new com.example.test_order_service.exception.ResourceNotFoundException("Test order not found"));

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/test-orders/{orderId}?page=1&size=6&keyword=", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isNotFound())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(404))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.error").value("Not Found"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[0]").value("Test order not found"));

            verify(testOrderService).deleteTestOrder(eq(nonExistentId), any(Pageable.class), anyString());
        }

        @Test
        @Order(25)
        @DisplayName("Should handle deletion of order with special characters in ID")
        public void deleteTestOrder_specialCharactersInId_success() throws Exception {
            // Given
            String orderId = "order-123-abc-xyz";

            TestOrderResponse testOrderResponse = TestOrderResponse.builder()
                    .testOrderId(orderId)
                    .patientName("Nguyen Van B")
                    .country("Thailand")
                    .status(TestOrderStatus.COMPLETED)
                    .build();

            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(List.of(testOrderResponse))
                    .build();

            when(testOrderService.deleteTestOrder(eq(orderId), any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/test-orders/{orderId}?page=1&size=6&keyword=", orderId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.items[0].testOrderId").value(orderId));

            verify(testOrderService).deleteTestOrder(eq(orderId), any(Pageable.class), anyString());
        }

        @Test
        @Order(26)
        @DisplayName("Should handle deletion of order with UUID format")
        public void deleteTestOrder_uuidFormat_success() throws Exception {
            // Given
            String uuidOrderId = "550e8400-e29b-41d4-a716-446655440000";

            TestOrderResponse testOrderResponse = TestOrderResponse.builder()
                    .testOrderId(uuidOrderId)
                    .patientName("Nguyen Van C")
                    .country("Singapore")
                    .status(TestOrderStatus.PENDING)
                    .build();

            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(List.of(testOrderResponse))
                    .build();

            when(testOrderService.deleteTestOrder(eq(uuidOrderId), any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            // When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .delete("/api/test-orders/{orderId}?page=1&size=6&keyword=", uuidOrderId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.statusCode").value(200))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.items[0].testOrderId").value(uuidOrderId));

            verify(testOrderService).deleteTestOrder(eq(uuidOrderId), any(Pageable.class), anyString());
        }
    }


    // ═══════════════════════════════════════════════════════════════
    // EDGE CASES AND ERROR SCENARIOS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EdgeCasesTests {

        @Test
        @Order(27)
        @DisplayName("Should handle empty request body for POST")
        public void createTestOrder_emptyRequestBody_fail() throws Exception {
            //Given
            String emptyJson = "{}";

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(emptyJson))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest());
        }

        @Test
        @Order(28)
        @DisplayName("Should handle empty request body for PUT")
        public void updateTestOrder_emptyRequestBody_success() throws Exception {
            //Given
            String orderId = "6b1f2e77-29d3-4e58-8a32-b9e7c3c3b1f0";
            String emptyJson = "{}";

            RestResponse<TestOrderResponse> updateRestResponse = RestResponse.<TestOrderResponse>builder()
                    .statusCode(200)
                    .result(testOrderResponse)
                    .message("Test order updated successfully")
                    .timestamp(LocalDateTime.now())
                    .build();

            when(testOrderService.updateTestOrder(eq(orderId), any(TestOrderUpdateRequest.class)))
                    .thenReturn(updateRestResponse);

            //When & Then - Empty body should be valid for PUT (all fields optional)
            mockMvc.perform(MockMvcRequestBuilders
                            .put("/api/test-orders/" + orderId)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(emptyJson))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @Order(29)
        @DisplayName("Should handle very long patient name")
        public void createTestOrder_veryLongPatientName_success() throws Exception {
            //Given
            String longName = "A".repeat(150); // Max length is 150
            testOrderRequest.setPatientName(longName);
            String requestContent = objectMapper.writeValueAsString(testOrderRequest);

            when(testOrderService.createTestOrder(any(TestOrderRequest.class)))
                    .thenReturn(restResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(requestContent))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @Order(30)
        @DisplayName("Should handle special characters in patient name")
        public void createTestOrder_specialCharactersInName_success() throws Exception {
            //Given
            testOrderRequest.setPatientName("Nguyễn Văn Ánh-Đức O'Brien");
            String requestContent = objectMapper.writeValueAsString(testOrderRequest);

            when(testOrderService.createTestOrder(any(TestOrderRequest.class)))
                    .thenReturn(restResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(requestContent))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @Order(31)
        @DisplayName("Should handle international phone number formats")
        public void createTestOrder_internationalPhoneNumber_success() throws Exception {
            //Given
            testOrderRequest.setPhone("+1 5551234567");
            String requestContent = objectMapper.writeValueAsString(testOrderRequest);

            when(testOrderService.createTestOrder(any(TestOrderRequest.class)))
                    .thenReturn(restResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(requestContent))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @Order(32)
        @DisplayName("Should handle future date of birth - should accept but may fail business logic")
        public void createTestOrder_futureDateOfBirth_processRequest() throws Exception {
            //Given
            testOrderRequest.setDateOfBirth(LocalDate.now().plusYears(1));
            String requestContent = objectMapper.writeValueAsString(testOrderRequest);

            when(testOrderService.createTestOrder(any(TestOrderRequest.class)))
                    .thenReturn(restResponse);

            //When & Then - Controller should accept, service validates business logic
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(requestContent))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @Order(33)
        @DisplayName("Should handle very old date of birth")
        public void createTestOrder_veryOldDateOfBirth_success() throws Exception {
            //Given
            testOrderRequest.setDateOfBirth(LocalDate.of(1900, 1, 1));
            String requestContent = objectMapper.writeValueAsString(testOrderRequest);

            when(testOrderService.createTestOrder(any(TestOrderRequest.class)))
                    .thenReturn(restResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(requestContent))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @Order(34)
        @DisplayName("Should handle NULL values in JSON for optional fields")
        public void createTestOrder_explicitNullValues_success() throws Exception {
            //Given
            String jsonWithNulls = """
                    {
                        "patientName": "John Doe",
                        "dateOfBirth": "01/01/1980",
                        "citizenId": "079399004953",
                        "country": "Vietnam",
                        "gender": "MALE",
                        "address": null,
                        "email": null,
                        "phone": "+84123456789"
                    }
                    """;

            when(testOrderService.createTestOrder(any(TestOrderRequest.class)))
                    .thenReturn(restResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(jsonWithNulls))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @Order(35)
        @DisplayName("Should handle multiple validation errors at once")
        public void createTestOrder_multipleValidationErrors_returnAllErrors() throws Exception {
            //Given
            testOrderRequest.setPatientName("");
            testOrderRequest.setCitizenId("");
            testOrderRequest.setCountry("");
            testOrderRequest.setPhone("123"); // Invalid format
            testOrderRequest.setEmail("invalid-email"); // Invalid format
            String invalidContent = objectMapper.writeValueAsString(testOrderRequest);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .content(invalidContent))
                    .andExpect(MockMvcResultMatchers.status().isBadRequest())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message.length()").value(5)) // 5 validation errors
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Patient name.*/)]").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Citizen ID.*/)]").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Country.*/)]").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Phone number.*/)]").exists())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.message[?(@ =~ /.*Email.*/)]").exists());
        }

        @Test
        @Order(36)
        @DisplayName("Should handle missing Content-Type header")
        public void createTestOrder_missingContentType_fail() throws Exception {
            //Given
            String validContent = objectMapper.writeValueAsString(testOrderRequest);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .post("/api/test-orders")
                            .content(validContent))
                    .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType());
        }

        @Test
        @Order(37)
        @DisplayName("Should handle GET request with very large page number")
        public void getTestOrders_veryLargePageNumber_returnEmpty() throws Exception {
            //Given
            PageResponse<TestOrderResponse> emptyPageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(9999)
                    .totalPages(10)
                    .items(Collections.emptyList())
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), anyString()))
                    .thenReturn(emptyPageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("page", "9999")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.result.items").isEmpty());
        }

        @Test
        @Order(38)
        @DisplayName("Should handle GET request with very large page size")
        public void getTestOrders_veryLargePageSize_success() throws Exception {
            //Given
            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(Collections.singletonList(testOrderResponse))
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), anyString()))
                    .thenReturn(pageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("size", "1000")
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }

        @Test
        @Order(39)
        @DisplayName("Should handle keyword with special regex characters")
        public void getTestOrders_keywordWithRegexCharacters_success() throws Exception {
            //Given
            String specialKeyword = "John.*Doe[test]";
            PageResponse<TestOrderResponse> pageResponse = PageResponse.<TestOrderResponse>builder()
                    .currentPage(1)
                    .totalPages(1)
                    .items(Collections.emptyList())
                    .build();

            when(testOrderService.getTestOrders(any(Pageable.class), eq(specialKeyword)))
                    .thenReturn(pageResponse);

            //When & Then
            mockMvc.perform(MockMvcRequestBuilders
                            .get("/api/test-orders")
                            .param("keyword", specialKeyword)
                            .contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(MockMvcResultMatchers.status().isOk());
        }
    }
}
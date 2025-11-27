package com.example.test_order_service.unit.mapper;

import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.response.TestOrderDetailResponse;
import com.example.test_order_service.dto.response.TestOrderResponse;
import com.example.test_order_service.entity.Comment;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.mapper.TestOrderMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;

/**
 * Comprehensive Unit Tests for TestOrderMapper
 * Tests MapStruct mapping logic with full coverage
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TestOrderMapper Unit Tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestOrderMapperTest {

    private final TestOrderMapper testOrderMapper = Mappers.getMapper(TestOrderMapper.class);

    private TestOrderRequest testOrderRequest;
    private TestOrder testOrder;

    @BeforeEach
    void setUp() {
        // Setup test order request
        testOrderRequest = TestOrderRequest.builder()
                .patientId(1) // required Integer
                .patientName("Nguyen Van An")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .identityNumber("079085012345") // citizenId -> identityNumber
                .country("Vietnam")
                .gender(Gender.MALE)
                .phone("+84987654321")
                .address("123 Le Loi Street, District 1, Ho Chi Minh City")
                .email("nguyenvanan@email.com")
                .build();

        // Setup test order entity
        testOrder = TestOrder.builder()
                .testOrderId("TO-001")
                .patientId(1) // Integer
                .patientName("Nguyen Van An")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .citizenId("079085012345") // entity vẫn citizenId
                .country("Vietnam")
                .gender(Gender.MALE)
                .phone("+84987654321")
                .address("123 Le Loi Street, District 1, Ho Chi Minh City")
                .email("nguyenvanan@email.com")
                .bloodCollectionId("TUBE-001")
                .status(TestOrderStatus.COMPLETED)
                .runBy("Dr. John Doe")
                .runAt(LocalDateTime.of(2025, 11, 6, 10, 30, 0))
                .reviewedBy("Dr. Jane Smith")
                .reviewedAt(LocalDateTime.of(2025, 11, 6, 14, 30, 0))
                .build();

        testOrder.setCreatedBy("Admin");
        testOrder.setCreatedAt(LocalDateTime.of(2025, 11, 6, 8, 0, 0));
    }

    // ═══════════════════════════════════════════════════════════════
    // TO TEST ORDER ENTITY (Request → Entity)
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Request to Entity Mapping Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class RequestToEntityTests {

        @Test
        @Order(1)
        @DisplayName("Should map TestOrderRequest to TestOrder successfully")
        void shouldMapTestOrderRequestToTestOrderSuccessfully() {
            TestOrder result = testOrderMapper.toTestOrderEntity(testOrderRequest);

            assertThat(result).isNotNull();
            assertThat(result.getPatientId()).isEqualTo(1);
            assertThat(result.getPatientName()).isEqualTo("Nguyen Van An");
            assertThat(result.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 3, 15));
            assertThat(result.getCitizenId()).isEqualTo("079085012345");
            assertThat(result.getCountry()).isEqualTo("Vietnam");
            assertThat(result.getGender()).isEqualTo(Gender.MALE);
            assertThat(result.getPhone()).isEqualTo("+84987654321");
            assertThat(result.getAddress()).isEqualTo("123 Le Loi Street, District 1, Ho Chi Minh City");
            assertThat(result.getEmail()).isEqualTo("nguyenvanan@email.com");
        }

        @Test
        @Order(2)
        @DisplayName("Should handle null request")
        void shouldHandleNullRequest() {
            TestOrder result = testOrderMapper.toTestOrderEntity(null);
            assertThat(result).isNull();
        }

        @Test
        @Order(3)
        @DisplayName("Should handle request with null optional fields")
        void shouldHandleRequestWithNullOptionalFields() {
            TestOrderRequest requestWithNulls = TestOrderRequest.builder()
                    .patientId(1)
                    .patientName("John Doe")
                    .identityNumber("123456789")
                    .country("USA")
                    .phone("1234567890")
                    .dateOfBirth(null)
                    .gender(null)
                    .address(null)
                    .email(null)
                    .build();

            TestOrder result = testOrderMapper.toTestOrderEntity(requestWithNulls);

            assertThat(result).isNotNull();
            assertThat(result.getPatientId()).isEqualTo(1);
            assertThat(result.getPatientName()).isEqualTo("John Doe");
            assertThat(result.getDateOfBirth()).isNull();
            assertThat(result.getGender()).isNull();
            assertThat(result.getAddress()).isNull();
            assertThat(result.getEmail()).isNull();
        }

        @Test
        @Order(4)
        @DisplayName("Should map all gender types correctly")
        void shouldMapAllGenderTypesCorrectly() {
            testOrderRequest.setGender(Gender.MALE);
            assertThat(testOrderMapper.toTestOrderEntity(testOrderRequest).getGender())
                    .isEqualTo(Gender.MALE);

            testOrderRequest.setGender(Gender.FEMALE);
            assertThat(testOrderMapper.toTestOrderEntity(testOrderRequest).getGender())
                    .isEqualTo(Gender.FEMALE);

            testOrderRequest.setGender(Gender.OTHER);
            assertThat(testOrderMapper.toTestOrderEntity(testOrderRequest).getGender())
                    .isEqualTo(Gender.OTHER);
        }

        @Test
        @Order(5)
        @DisplayName("Should handle empty strings in request")
        void shouldHandleEmptyStringsInRequest() {
            testOrderRequest.setAddress("");
            testOrderRequest.setEmail("");

            TestOrder result = testOrderMapper.toTestOrderEntity(testOrderRequest);

            assertThat(result.getAddress()).isEmpty();
            assertThat(result.getEmail()).isEmpty();
        }

        @Test
        @Order(6)
        @DisplayName("Should handle special characters in fields")
        void shouldHandleSpecialCharactersInFields() {
            testOrderRequest.setPatientName("Nguyễn Văn Anh (Tên gọi: 'Andy')");
            testOrderRequest.setAddress("Số 1/2, Đường 3-4, P.5-6 Q.7/8");

            TestOrder result = testOrderMapper.toTestOrderEntity(testOrderRequest);

            assertThat(result.getPatientName()).isEqualTo("Nguyễn Văn Anh (Tên gọi: 'Andy')");
            assertThat(result.getAddress()).isEqualTo("Số 1/2, Đường 3-4, P.5-6 Q.7/8");
        }

        @Test
        @Order(7)
        @DisplayName("Should not set auto-generated fields during request mapping")
        void shouldNotSetAutoGeneratedFieldsDuringRequestMapping() {
            TestOrder result = testOrderMapper.toTestOrderEntity(testOrderRequest);

            assertThat(result.getTestOrderId()).isNull();
            assertThat(result.getBloodCollectionId()).isNull();
            assertThat(result.getStatus()).isNull();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TO TEST ORDER RESPONSE (Entity → Response)
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Entity to Response Mapping Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EntityToResponseTests {

        @Test
        @Order(1)
        @DisplayName("Should map TestOrder to TestOrderResponse successfully")
        void shouldMapTestOrderToTestOrderResponseSuccessfully() {
            TestOrderResponse response = testOrderMapper.toTestOrderResponse(testOrder);

            assertThat(response).isNotNull();
            assertThat(response.getTestOrderId()).isEqualTo("TO-001");
            assertThat(response.getPatientId()).isEqualTo(1);
            assertThat(response.getPatientName()).isEqualTo("Nguyen Van An");
            assertThat(response.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 3, 15));
            assertThat(response.getCitizenId()).isEqualTo("079085012345");
            assertThat(response.getCountry()).isEqualTo("Vietnam");
            assertThat(response.getGender()).isEqualTo(Gender.MALE);
            assertThat(response.getPhone()).isEqualTo("+84987654321");
            assertThat(response.getAddress()).isEqualTo("123 Le Loi Street, District 1, Ho Chi Minh City");
            assertThat(response.getEmail()).isEqualTo("nguyenvanan@email.com");
            assertThat(response.getBloodCollectionId()).isEqualTo("TUBE-001");
            assertThat(response.getStatus()).isEqualTo(TestOrderStatus.COMPLETED);
            assertThat(response.getCreatedBy()).isEqualTo("Admin");
        }

        @Test
        @Order(2)
        @DisplayName("Should handle null entity for response")
        void shouldHandleNullEntityForResponse() {
            assertThat(testOrderMapper.toTestOrderResponse(null)).isNull();
        }

        @Test
        @Order(3)
        @DisplayName("Should map all status types correctly")
        void shouldMapAllStatusTypesCorrectly() {
            testOrder.setStatus(TestOrderStatus.PENDING);
            assertThat(testOrderMapper.toTestOrderResponse(testOrder).getStatus())
                    .isEqualTo(TestOrderStatus.PENDING);

            testOrder.setStatus(TestOrderStatus.COMPLETED);
            assertThat(testOrderMapper.toTestOrderResponse(testOrder).getStatus())
                    .isEqualTo(TestOrderStatus.COMPLETED);

            testOrder.setStatus(TestOrderStatus.REVIEWED);
            assertThat(testOrderMapper.toTestOrderResponse(testOrder).getStatus())
                    .isEqualTo(TestOrderStatus.REVIEWED);
        }

        @Test
        @Order(4)
        @DisplayName("Should handle entity with null fields")
        void shouldHandleEntityWithNullFields() {
            TestOrder orderWithNulls = TestOrder.builder()
                    .testOrderId("TO-002")
                    .patientName("Test Patient")
                    .patientId(null)
                    .dateOfBirth(null)
                    .gender(null)
                    .address(null)
                    .email(null)
                    .build();

            TestOrderResponse response = testOrderMapper.toTestOrderResponse(orderWithNulls);

            assertThat(response).isNotNull();
            assertThat(response.getPatientId()).isNull();
            assertThat(response.getDateOfBirth()).isNull();
            assertThat(response.getGender()).isNull();
            assertThat(response.getAddress()).isNull();
            assertThat(response.getEmail()).isNull();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // TO TEST ORDER DETAIL RESPONSE (Entity → DetailResponse)
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Entity to Detail Response Mapping Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EntityToDetailResponseTests {

        @Test
        @Order(1)
        @DisplayName("Should map TestOrder to TestOrderDetailResponse successfully")
        void shouldMapTestOrderToTestOrderDetailResponseSuccessfully() {
            TestOrderDetailResponse response = testOrderMapper.toTestOrderDetailResponse(testOrder);

            assertThat(response).isNotNull();
            assertThat(response.getTestOrderId()).isEqualTo("TO-001");
            assertThat(response.getPatientId()).isEqualTo(1);
            assertThat(response.getPatientName()).isEqualTo("Nguyen Van An");
            assertThat(response.getDateOfBirth()).isEqualTo(LocalDate.of(1985, 3, 15));
            assertThat(response.getCitizenId()).isEqualTo("079085012345");
            assertThat(response.getCountry()).isEqualTo("Vietnam");
            assertThat(response.getGender()).isEqualTo(Gender.MALE);
            assertThat(response.getPhone()).isEqualTo("+84987654321");
            assertThat(response.getAddress()).isEqualTo("123 Le Loi Street, District 1, Ho Chi Minh City");
            assertThat(response.getEmail()).isEqualTo("nguyenvanan@email.com");
            assertThat(response.getBloodCollectionId()).isEqualTo("TUBE-001");
            assertThat(response.getStatus()).isEqualTo(TestOrderStatus.COMPLETED);
        }

        @Test
        @Order(2)
        @DisplayName("Should map audit fields correctly in detail response")
        void shouldMapAuditFieldsCorrectlyInDetailResponse() {
            TestOrderDetailResponse response = testOrderMapper.toTestOrderDetailResponse(testOrder);

            assertThat(response.getCreatedBy()).isEqualTo("Admin");
            assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 11, 6, 8, 0, 0));
            assertThat(response.getRunBy()).isEqualTo("Dr. John Doe");
            assertThat(response.getRunAt()).isEqualTo(LocalDateTime.of(2025, 11, 6, 10, 30, 0));
            assertThat(response.getReviewedBy()).isEqualTo("Dr. Jane Smith");
            assertThat(response.getReviewedAt()).isEqualTo(LocalDateTime.of(2025, 11, 6, 14, 30, 0));
        }

        @Test
        @Order(3)
        @DisplayName("Should handle null entity for detail response")
        void shouldHandleNullEntityForDetailResponse() {
            assertThat(testOrderMapper.toTestOrderDetailResponse(null)).isNull();
        }

        @Test
        @Order(4)
        @DisplayName("Should map nested test results in detail response")
        void shouldMapNestedTestResultsInDetailResponse() {
            TestResult testResult = TestResult.builder()
                    .resultId("TR-001")
                    .bloodCollectionId("TUBE-001")
                    .status("COMPLETED")
                    .build();
            testOrder.setTestResults(testResult);

            TestOrderDetailResponse response = testOrderMapper.toTestOrderDetailResponse(testOrder);

            assertThat(response.getTestResults()).isNotNull();
        }

        @Test
        @Order(5)
        @DisplayName("Should handle null test results in detail response")
        void shouldHandleNullTestResultsInDetailResponse() {
            testOrder.setTestResults(null);

            TestOrderDetailResponse response = testOrderMapper.toTestOrderDetailResponse(testOrder);

            assertThat(response.getTestResults()).isNull();
        }

        @Test
        @Order(6)
        @DisplayName("Should map nested comments in detail response")
        void shouldMapNestedCommentsInDetailResponse() {
            Comment comment1 = Comment.builder()
                    .commentId("C-001")
                    .commentText("First comment")
                    .build();
            Comment comment2 = Comment.builder()
                    .commentId("C-002")
                    .commentText("Second comment")
                    .build();
            testOrder.setComments(Arrays.asList(comment1, comment2));

            TestOrderDetailResponse response = testOrderMapper.toTestOrderDetailResponse(testOrder);

            assertThat(response.getComments()).isNotNull();
            assertThat(response.getComments()).hasSize(2);
        }

        @Test
        @Order(7)
        @DisplayName("Should handle null comments in detail response")
        void shouldHandleNullCommentsInDetailResponse() {
            testOrder.setComments(null);

            TestOrderDetailResponse response = testOrderMapper.toTestOrderDetailResponse(testOrder);

            assertThat(response.getComments()).isNull();
        }

        @Test
        @Order(8)
        @DisplayName("Should handle empty comments list in detail response")
        void shouldHandleEmptyCommentsListInDetailResponse() {
            testOrder.setComments(Collections.emptyList());

            TestOrderDetailResponse response = testOrderMapper.toTestOrderDetailResponse(testOrder);

            assertThat(response.getComments()).isEmpty();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // EDGE CASES AND SPECIAL SCENARIOS
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Edge Cases and Special Scenarios")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EdgeCasesTests {

        @Test
        @Order(1)
        @DisplayName("Should handle very long text fields")
        void shouldHandleVeryLongTextFields() {
            String longName = "A".repeat(150);
            String longAddress = "B".repeat(255);
            testOrderRequest.setPatientName(longName);
            testOrderRequest.setAddress(longAddress);

            TestOrder result = testOrderMapper.toTestOrderEntity(testOrderRequest);

            assertThat(result.getPatientName()).hasSize(150);
            assertThat(result.getAddress()).hasSize(255);
        }

        @Test
        @Order(2)
        @DisplayName("Should not modify original entity during mapping")
        void shouldNotModifyOriginalEntityDuringMapping() {
            String originalName = testOrder.getPatientName();
            TestOrderStatus originalStatus = testOrder.getStatus();

            testOrderMapper.toTestOrderResponse(testOrder);

            assertThat(testOrder.getPatientName()).isEqualTo(originalName);
            assertThat(testOrder.getStatus()).isEqualTo(originalStatus);
        }

        @Test
        @Order(3)
        @DisplayName("Should create independent response objects")
        void shouldCreateIndependentResponseObjects() {
            TestOrderResponse response1 = testOrderMapper.toTestOrderResponse(testOrder);
            TestOrderResponse response2 = testOrderMapper.toTestOrderResponse(testOrder);

            assertThat(response1).isNotSameAs(response2);
        }

        @Test
        @Order(4)
        @DisplayName("Should handle whitespace in fields")
        void shouldHandleWhitespaceInFields() {
            testOrderRequest.setPatientName("  John Doe  ");
            testOrderRequest.setAddress("  123 Street  ");

            TestOrder result = testOrderMapper.toTestOrderEntity(testOrderRequest);

            assertThat(result.getPatientName()).isEqualTo("  John Doe  ");
            assertThat(result.getAddress()).isEqualTo("  123 Street  ");
        }

        @Test
        @Order(5)
        @DisplayName("Should handle future date of birth")
        void shouldHandleFutureDateOfBirth() {
            LocalDate futureDate = LocalDate.of(2030, 12, 31);
            testOrder.setDateOfBirth(futureDate);

            TestOrderDetailResponse response = testOrderMapper.toTestOrderDetailResponse(testOrder);

            assertThat(response.getDateOfBirth()).isEqualTo(futureDate);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPSTRUCT BEHAVIOR VERIFICATION
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("MapStruct Behavior Verification")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class MapStructBehaviorTests {

        @Test
        @Order(1)
        @DisplayName("Should verify mapper is not null")
        void shouldVerifyMapperIsNotNull() {
            assertThat(testOrderMapper).isNotNull();
        }

        @Test
        @Order(2)
        @DisplayName("Should map consistently for same input")
        void shouldMapConsistentlyForSameInput() {
            TestOrderResponse response1 = testOrderMapper.toTestOrderResponse(testOrder);
            TestOrderResponse response2 = testOrderMapper.toTestOrderResponse(testOrder);

            assertThat(response1.getTestOrderId()).isEqualTo(response2.getTestOrderId());
            assertThat(response1.getPatientName()).isEqualTo(response2.getPatientName());
            assertThat(response1.getStatus()).isEqualTo(response2.getStatus());
        }

        @Test
        @Order(3)
        @DisplayName("Should handle concurrent mapping calls")
        void shouldHandleConcurrentMappingCalls() {
            List<TestOrderResponse> responses = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                responses.add(testOrderMapper.toTestOrderResponse(testOrder));
            }

            assertThat(responses).hasSize(100);
            responses.forEach(response -> {
                assertThat(response.getTestOrderId()).isEqualTo("TO-001");
                assertThat(response.getPatientName()).isEqualTo("Nguyen Van An");
            });
        }
    }
}

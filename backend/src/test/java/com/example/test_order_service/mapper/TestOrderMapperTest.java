package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.dto.response.TestOrderDetailResponse;
import com.example.test_order_service.dto.response.TestOrderResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TestOrderMapperTest {

    private final TestOrderMapper mapper = Mappers.getMapper(TestOrderMapper.class);

    @Test
    void toTestOrderEntity_shouldMapFromRequest() {
        TestOrderRequest req = TestOrderRequest.builder()
                .patientName("Nguyen Van A")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .citizenId("123")
                .country("VN")
                .gender(Gender.MALE)
                .address("HN")
                .email("a@gmail.com")
                .phone("0123456789")
                .build();

        TestOrder entity = mapper.toTestOrderEntity(req);

        assertNotNull(entity);
        assertEquals("Nguyen Van A", entity.getPatientName());
        assertEquals(LocalDate.of(2000, 1, 1), entity.getDateOfBirth());
        assertEquals("123", entity.getCitizenId());
        assertEquals("VN", entity.getCountry());
        assertEquals(Gender.MALE, entity.getGender());
        assertEquals("HN", entity.getAddress());
        assertEquals("a@gmail.com", entity.getEmail());
        assertEquals("0123456789", entity.getPhone());
    }

    @Test
    void toTestOrderEntity_nullRequest_shouldReturnNull() {
        assertNull(mapper.toTestOrderEntity(null));
    }

    @Test
    void toTestOrderResponse_shouldMapEntityToDto() {
        LocalDateTime now = LocalDateTime.now();
        TestOrder o = new TestOrder();
        o.setTestOrderId("to1");
        o.setPatientId("p1");
        o.setPatientName("A");
        o.setDateOfBirth(LocalDate.of(2000, 1, 1));
        o.setCitizenId("123");
        o.setCountry("VN");
        o.setGender(Gender.FEMALE);
        o.setAddress("HN");
        o.setEmail("a@gmail.com");
        o.setPhone("0123");
        o.setBloodCollectionId("bc1");
        o.setCreatedBy("admin");
        o.setCreatedAt(now);
        o.setStatus(TestOrderStatus.PENDING);

        TestOrderResponse dto = mapper.toTestOrderResponse(o);

        assertNotNull(dto);
        assertEquals("to1", dto.getTestOrderId());
        assertEquals("p1", dto.getPatientId());
        assertEquals("A", dto.getPatientName());
        assertEquals(LocalDate.of(2000, 1, 1), dto.getDateOfBirth());
        assertEquals("123", dto.getCitizenId());
        assertEquals("VN", dto.getCountry());
        assertEquals(Gender.FEMALE, dto.getGender());
        assertEquals("HN", dto.getAddress());
        assertEquals("a@gmail.com", dto.getEmail());
        assertEquals("0123", dto.getPhone());
        assertEquals("bc1", dto.getBloodCollectionId());
        assertEquals(TestOrderStatus.PENDING, dto.getStatus());
        assertEquals("admin", dto.getCreatedBy());
        assertEquals(now, dto.getCreatedAt());
    }

    @Test
    void toTestOrderResponse_nullEntity_shouldReturnNull() {
        assertNull(mapper.toTestOrderResponse(null));
    }

    @Test
    void toTestOrderResponse_whenNullableFieldsNull_shouldMapAndKeepNulls() {
        TestOrder o = new TestOrder();
        o.setTestOrderId("to-null");
        o.setPatientId("p-null");
        o.setPatientName("NullCase");
        o.setGender(null);
        o.setStatus(null);
        o.setCreatedAt(null);
        o.setCreatedBy(null);
        o.setBloodCollectionId(null);

        TestOrderResponse dto = mapper.toTestOrderResponse(o);

        assertNotNull(dto);
        assertEquals("to-null", dto.getTestOrderId());
        assertEquals("p-null", dto.getPatientId());
        assertEquals("NullCase", dto.getPatientName());
        assertNull(dto.getGender());
        assertNull(dto.getStatus());
        assertNull(dto.getCreatedAt());
        assertNull(dto.getCreatedBy());
        assertNull(dto.getBloodCollectionId());
    }

    @Test
    void toTestOrderDetailResponse_shouldMapEntityToDetailDto() {
        TestOrder o = new TestOrder();
        o.setTestOrderId("to1");
        o.setPatientId("p1");
        o.setPatientName("A");
        o.setCitizenId("123");
        o.setCountry("VN");
        o.setGender(Gender.MALE);
        o.setBloodCollectionId("bc1");
        o.setStatus(TestOrderStatus.COMPLETED);

        TestOrderDetailResponse dto = mapper.toTestOrderDetailResponse(o);

        assertNotNull(dto);
        assertEquals("to1", dto.getTestOrderId());
        assertEquals("p1", dto.getPatientId());
        assertEquals("A", dto.getPatientName());
        assertEquals("123", dto.getCitizenId());
        assertEquals("VN", dto.getCountry());
        assertEquals(Gender.MALE, dto.getGender());
        assertEquals("bc1", dto.getBloodCollectionId());
        assertEquals(TestOrderStatus.COMPLETED, dto.getStatus());
    }

    @Test
    void toTestOrderDetailResponse_nullEntity_shouldReturnNull() {
        assertNull(mapper.toTestOrderDetailResponse(null));
    }

    @Test
    void toTestOrderDetailResponse_whenNullableFieldsNull_shouldMapAndKeepNulls() {
        TestOrder o = new TestOrder();
        o.setTestOrderId("to-null-detail");
        o.setPatientId("p-null-detail");
        o.setPatientName("DetailNull");
        o.setCitizenId(null);
        o.setCountry(null);
        o.setGender(null);
        o.setBloodCollectionId(null);
        o.setStatus(null);

        TestOrderDetailResponse dto = mapper.toTestOrderDetailResponse(o);

        assertNotNull(dto);
        assertEquals("to-null-detail", dto.getTestOrderId());
        assertEquals("p-null-detail", dto.getPatientId());
        assertEquals("DetailNull", dto.getPatientName());
        assertNull(dto.getCitizenId());
        assertNull(dto.getCountry());
        assertNull(dto.getGender());
        assertNull(dto.getBloodCollectionId());
        assertNull(dto.getStatus());
    }
}

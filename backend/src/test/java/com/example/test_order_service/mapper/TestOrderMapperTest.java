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
                .dateOfBirth(LocalDate.of(2000,1,1))
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
        assertEquals(LocalDate.of(2000,1,1), entity.getDateOfBirth());
        assertEquals("123", entity.getCitizenId());
        assertEquals("VN", entity.getCountry());
        assertEquals(Gender.MALE, entity.getGender());
        assertEquals("HN", entity.getAddress());
        assertEquals("a@gmail.com", entity.getEmail());
        assertEquals("0123456789", entity.getPhone());
    }

    @Test
    void toTestOrderResponse_shouldMapEntityToDto() {
        LocalDateTime now = LocalDateTime.now();
        TestOrder o = new TestOrder();
        o.setTestOrderId("to1");
        o.setPatientId("p1");
        o.setPatientName("A");
        o.setDateOfBirth(LocalDate.of(2000,1,1));
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
        assertEquals("123", dto.getCitizenId());
        assertEquals("bc1", dto.getBloodCollectionId());
        assertEquals(TestOrderStatus.PENDING, dto.getStatus());
        assertEquals(now, dto.getCreatedAt());
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
}

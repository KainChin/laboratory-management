package com.example.test_order_service.unit.mapper;

import com.example.test_order_service.dto.repsonse.TestOrderDetailResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.enumForEntity.Gender;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.mapper.TestOrderMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class TestOrderMapperTest {

    private final TestOrderMapper testOrderMapper = Mappers.getMapper(TestOrderMapper.class);

    @Test
    void toTestOrderEntity() {
        TestOrderRequest request = new TestOrderRequest();
        request.setPatientName("John Doe");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setGender(Gender.MALE);
        request.setCitizenId("123456789");
        request.setCountry("USA");

        TestOrder testOrder = testOrderMapper.toTestOrderEntity(request);

        assertThat(testOrder).isNotNull();
        assertThat(testOrder.getPatientName()).isEqualTo("John Doe");
        assertThat(testOrder.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 1, 1));
        assertThat(testOrder.getGender()).isEqualTo(Gender.MALE);
        assertThat(testOrder.getCitizenId()).isEqualTo("123456789");
        assertThat(testOrder.getCountry()).isEqualTo("USA");
    }

    @Test
    void toTestOrderResponse() {
        TestOrder testOrder = new TestOrder();
        testOrder.setTestOrderId("T001");
        testOrder.setPatientName("Jane Doe");
        testOrder.setStatus(TestOrderStatus.PENDING);

        TestOrderResponse response = testOrderMapper.toTestOrderResponse(testOrder);

        assertThat(response).isNotNull();
        assertThat(response.getTestOrderId()).isEqualTo("T001");
        assertThat(response.getPatientName()).isEqualTo("Jane Doe");
        assertThat(response.getStatus()).isEqualTo(TestOrderStatus.PENDING);
    }

    @Test
    void toTestOrderDetailResponse() {
        TestOrder testOrder = new TestOrder();
        testOrder.setTestOrderId("T001");
        testOrder.setPatientName("Jane Doe");
        testOrder.setDateOfBirth(LocalDate.of(1992, 2, 2));
        testOrder.setGender(Gender.FEMALE);
        testOrder.setCitizenId("987654321");
        testOrder.setCountry("Canada");
        testOrder.setStatus(TestOrderStatus.COMPLETED);

        TestOrderDetailResponse response = testOrderMapper.toTestOrderDetailResponse(testOrder);

        assertThat(response).isNotNull();
        assertThat(response.getTestOrderId()).isEqualTo("T001");
        assertThat(response.getPatientName()).isEqualTo("Jane Doe");
        assertThat(response.getDateOfBirth()).isEqualTo(LocalDate.of(1992, 2, 2));
        assertThat(response.getGender()).isEqualTo(Gender.FEMALE);
        assertThat(response.getCitizenId()).isEqualTo("987654321");
        assertThat(response.getCountry()).isEqualTo("Canada");
        assertThat(response.getStatus()).isEqualTo(TestOrderStatus.COMPLETED);
    }
}


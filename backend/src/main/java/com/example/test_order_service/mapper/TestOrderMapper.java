package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.response.TestOrderDetailResponse;
import com.example.test_order_service.dto.response.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.entity.TestOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TestOrderMapper {
    @Mapping(source = "identityNumber", target = "citizenId")
    TestOrder toTestOrderEntity(TestOrderRequest request);

    @Mapping(source = "citizenId", target = "identityNumber")
    TestOrderResponse toTestOrderResponse(TestOrder testOrder);

    @Mapping(source = "citizenId", target = "identityNumber")
    TestOrderDetailResponse toTestOrderDetailResponse(TestOrder testOrder);
}

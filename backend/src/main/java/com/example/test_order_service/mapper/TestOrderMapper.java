package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.repsonse.TestOrderDetailResponse;
import com.example.test_order_service.dto.repsonse.TestOrderResponse;
import com.example.test_order_service.dto.request.TestOrderRequest;
import com.example.test_order_service.entity.TestOrder;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TestOrderMapper {
    TestOrder toTestOrderEntity(TestOrderRequest request);
    TestOrderResponse toTestOrderResponse(TestOrder testOrder);
    TestOrderDetailResponse toTestOrderDetailResponse(TestOrder testOrder);
}

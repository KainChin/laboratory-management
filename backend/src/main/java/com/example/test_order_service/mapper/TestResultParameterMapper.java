package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.response.TestResultParameterResponse;
import com.example.test_order_service.entity.TestResultParameter;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TestResultParameterMapper {
    TestResultParameterResponse toTestResultParameterResponse(TestResultParameter testResultParameter);
}
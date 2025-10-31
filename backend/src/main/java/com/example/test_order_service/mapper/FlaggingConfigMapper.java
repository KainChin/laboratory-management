package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.repsonse.FlaggingConfigResponse;
import com.example.test_order_service.entity.FlaggingConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FlaggingConfigMapper {
    FlaggingConfigResponse toFlaggingConfigResponse(FlaggingConfig flaggingConfig);
    List<FlaggingConfigResponse> toFlaggingConfigListResponse(List<FlaggingConfig> flaggingConfigList);
}

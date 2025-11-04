package com.example.test_order_service.unit.mapper;

import com.example.test_order_service.dto.response.FlaggingConfigResponse;
import com.example.test_order_service.entity.FlaggingConfig;
import com.example.test_order_service.mapper.FlaggingConfigMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FlaggingConfigMapperTest {

    private final FlaggingConfigMapper flaggingConfigMapper = Mappers.getMapper(FlaggingConfigMapper.class);

    @Test
    void toFlaggingConfigResponse() {
        FlaggingConfig config = new FlaggingConfig();
        config.setConfigId("1");
        config.setParameter("age");
        config.setMinValue(60.0);
        config.setMaxValue(100.0);
        config.setUnit("years");

        FlaggingConfigResponse response = flaggingConfigMapper.toFlaggingConfigResponse(config);

        assertThat(response).isNotNull();
        assertThat(response.getConfigId()).isEqualTo("1");
        assertThat(response.getParameter()).isEqualTo("age");
        assertThat(response.getMinValue()).isEqualTo(60.0);
        assertThat(response.getMaxValue()).isEqualTo(100.0);
        assertThat(response.getUnit()).isEqualTo("years");
    }

    @Test
    void toFlaggingConfigListResponse() {
        FlaggingConfig config = new FlaggingConfig();
        config.setConfigId("1");
        config.setParameter("age");
        List<FlaggingConfig> configs = Collections.singletonList(config);

        List<FlaggingConfigResponse> responses = flaggingConfigMapper.toFlaggingConfigListResponse(configs);

        assertThat(responses).isNotNull();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getConfigId()).isEqualTo("1");
        assertThat(responses.get(0).getParameter()).isEqualTo("age");
    }
}
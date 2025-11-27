package com.example.test_order_service.mapper;

import com.example.test_order_service.dto.response.FlaggingConfigResponse;
import com.example.test_order_service.entity.FlaggingConfig;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlaggingConfigMapperTest {

    private final FlaggingConfigMapper mapper = Mappers.getMapper(FlaggingConfigMapper.class);

    @Test
    void toFlaggingConfigResponse_shouldMapEntityToDto() {
        FlaggingConfig cfg = new FlaggingConfig();
        cfg.setConfigId("cfg1");
        cfg.setParameter("GLU");
        cfg.setMinValue(3.5);
        cfg.setMaxValue(6.5);
        cfg.setUnit("mmol/L");
        cfg.setUpdatedAt(LocalDateTime.now());

        FlaggingConfigResponse dto = mapper.toFlaggingConfigResponse(cfg);

        assertNotNull(dto);
        assertEquals("cfg1", dto.getConfigId());
        assertEquals("GLU", dto.getParameter());
        assertEquals(3.5, dto.getMinValue());
        assertEquals(6.5, dto.getMaxValue());
        assertEquals("mmol/L", dto.getUnit());
    }

    @Test
    void toFlaggingConfigListResponse_shouldMapList() {
        FlaggingConfig a = new FlaggingConfig();
        a.setConfigId("1");
        a.setParameter("A");

        FlaggingConfig b = new FlaggingConfig();
        b.setConfigId("2");
        b.setParameter("B");

        List<FlaggingConfigResponse> dtos =
                mapper.toFlaggingConfigListResponse(List.of(a, b));

        assertEquals(2, dtos.size());
        assertEquals("1", dtos.get(0).getConfigId());
        assertEquals("2", dtos.get(1).getConfigId());
    }
}

package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.response.FlaggingConfigResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.entity.FlaggingConfig;
import com.example.test_order_service.mapper.FlaggingConfigMapper;
import com.example.test_order_service.repository.FlaggingConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FlaggingConfigServiceImplTest {

    @Mock FlaggingConfigRepository repository;
    @Mock FlaggingConfigMapper mapper;

    @InjectMocks FlaggingConfigServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getFlaggingConfigs_shouldReturnMappedListAnd200() {
        List<FlaggingConfig> entities = List.of(
                new FlaggingConfig("cfg1","GLU",3.0,6.0,"mmol/L", null)
        );
        List<FlaggingConfigResponse> dtos = List.of(
                FlaggingConfigResponse.builder().configId("cfg1").parameter("GLU").build()
        );

        when(repository.findAll()).thenReturn(entities);
        when(mapper.toFlaggingConfigListResponse(entities)).thenReturn(dtos);

        RestResponse<List<FlaggingConfigResponse>> res = service.getFlaggingConfigs();

        assertEquals(200, res.getStatusCode());
        assertEquals("Flagging configs retrieved successfully", res.getMessage());
        assertEquals(1, res.getResult().size());

        verify(repository).findAll();
        verify(mapper).toFlaggingConfigListResponse(entities);
    }
}

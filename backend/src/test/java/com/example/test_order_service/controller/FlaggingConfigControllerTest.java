package com.example.test_order_service.controller;

import com.example.test_order_service.dto.response.FlaggingConfigResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.service.FlaggingConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlaggingConfigController.class)
@AutoConfigureMockMvc(addFilters = false)
class FlaggingConfigControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean FlaggingConfigService flaggingConfigService;

    @Test
    void getFlaggingConfigs_shouldReturnListFromService() throws Exception {
        List<FlaggingConfigResponse> list = List.of(
                FlaggingConfigResponse.builder().parameter("WBC").minValue(1.0).maxValue(10.0).build()
        );

        RestResponse<List<FlaggingConfigResponse>> serviceRes =
                RestResponse.<List<FlaggingConfigResponse>>builder()
                        .statusCode(200)
                        .message("ok")
                        .result(list)
                        .timestamp(LocalDateTime.now())
                        .build();

        when(flaggingConfigService.getFlaggingConfigs()).thenReturn(serviceRes);

        mockMvc.perform(get("/api/test-orders/flagging-configs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.result[0].parameter").value("WBC"));

        verify(flaggingConfigService).getFlaggingConfigs();
    }
}

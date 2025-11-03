package com.example.test_order_service.controller;

import com.example.test_order_service.dto.response.FlaggingConfigResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.service.FlaggingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test-orders/flagging-configs")
@RequiredArgsConstructor
public class FlaggingConfigController {
    private final FlaggingConfigService flaggingConfigService;

    @GetMapping
    public RestResponse<List<FlaggingConfigResponse>> getFlaggingConfigs() {
        return flaggingConfigService.getFlaggingConfigs();
    }
}

package com.example.test_order_service.service;

import com.example.test_order_service.dto.response.FlaggingConfigResponse;
import com.example.test_order_service.dto.response.RestResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface FlaggingConfigService {
    RestResponse<List<FlaggingConfigResponse>> getFlaggingConfigs();
}

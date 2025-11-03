package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.response.FlaggingConfigResponse;
import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.mapper.FlaggingConfigMapper;
import com.example.test_order_service.repository.FlaggingConfigRepository;
import com.example.test_order_service.service.FlaggingConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlaggingConfigServiceImpl implements FlaggingConfigService {
    private final FlaggingConfigRepository flaggingConfigRepository;
    private final FlaggingConfigMapper flaggingConfigMapper;

    @Override
    public RestResponse<List<FlaggingConfigResponse>> getFlaggingConfigs() {
        return RestResponse.<List<FlaggingConfigResponse>>builder()
                .statusCode(200)
                .result(flaggingConfigMapper.toFlaggingConfigListResponse(flaggingConfigRepository.findAll()))
                .message("Flagging configs retrieved successfully")
                .timestamp(LocalDateTime.now())
                .build();
    }
}

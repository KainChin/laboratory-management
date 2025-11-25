package com.example.test_order_service.controller;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.service.TestResultService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TestResultControllerTest {

    @Mock private TestResultService testResultService;

    @InjectMocks private TestResultController testResultController;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(testResultController).build();
    }

    @Test
    void receiveHl7_shouldDelegateToService() throws Exception {
        String hl7 = "MSH|^~\\&|...";

        RestResponse<TestResultResponse> serviceRes =
                RestResponse.<TestResultResponse>builder()
                        .statusCode(200)
                        .message("processed")
                        .result(new TestResultResponse())
                        .build();

        doReturn(serviceRes).when(testResultService).receiveHl7(anyString());

        mockMvc.perform(post("/api/test-results/hl7")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(hl7))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.message").value("processed"));

        verify(testResultService).receiveHl7(hl7);
    }

    @Test
    void getResultByBloodCollectionId_shouldDelegateToService() throws Exception {
        RestResponse<?> serviceRes =
                RestResponse.builder()
                        .statusCode(200)
                        .message("ok")
                        .result("anything")
                        .build();

        doReturn(serviceRes).when(testResultService).getResultByBloodCollectionId("BCT-1");

        mockMvc.perform(get("/api/test-results/BCT-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("ok"));

        verify(testResultService).getResultByBloodCollectionId("BCT-1");
    }
}

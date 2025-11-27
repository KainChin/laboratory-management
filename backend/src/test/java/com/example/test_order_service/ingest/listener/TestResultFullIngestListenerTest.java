package com.example.test_order_service.ingest.listener;

import com.example.test_order_service.service.TestResultService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TestResultFullIngestListenerTest {

    @Mock
    private TestResultService testResultService;

    @InjectMocks
    private TestResultFullIngestListener listener;

    @Test
    void onMessage_null_shouldSkipAndNotCallService() {
        assertDoesNotThrow(() -> listener.onMessage(null));
        verifyNoInteractions(testResultService);
    }

    @Test
    void onMessage_blank_shouldSkipAndNotCallService() {
        assertDoesNotThrow(() -> listener.onMessage("   "));
        verifyNoInteractions(testResultService);
    }

    @Test
    void onMessage_valid_shouldDelegateToService() {
        String hl7 = "MSH|^~\\&|...";

        assertDoesNotThrow(() -> listener.onMessage(hl7));

        verify(testResultService, times(1)).receiveHl7(hl7);
        verifyNoMoreInteractions(testResultService);
    }

    @Test
    void onMessage_serviceThrowsIllegalArgumentException_shouldBeCaughtAndNotRethrow() {
        String hl7 = "MSH|^~\\&|INVALID";

        doThrow(new IllegalArgumentException("duplicate"))
                .when(testResultService).receiveHl7(hl7);

        assertDoesNotThrow(() -> listener.onMessage(hl7));

        verify(testResultService, times(1)).receiveHl7(hl7);
    }

    @Test
    void onMessage_serviceThrowsUnexpectedException_shouldBeCaughtAndNotRethrow() {
        String hl7 = "MSH|^~\\&|CRASH";

        doThrow(new RuntimeException("db down"))
                .when(testResultService).receiveHl7(hl7);

        assertDoesNotThrow(() -> listener.onMessage(hl7));

        verify(testResultService, times(1)).receiveHl7(hl7);
    }
}

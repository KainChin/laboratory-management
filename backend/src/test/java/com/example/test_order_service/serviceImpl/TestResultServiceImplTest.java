package com.example.test_order_service.serviceImpl;

import com.example.test_order_service.dto.response.RestResponse;
import com.example.test_order_service.dto.response.TestResultResponse;
import com.example.test_order_service.entity.TestOrder;
import com.example.test_order_service.entity.TestResult;
import com.example.test_order_service.entity.enumForEntity.TestOrderStatus;
import com.example.test_order_service.exception.ResourceNotFoundException;
import com.example.test_order_service.mapper.TestResultMapper;
import com.example.test_order_service.repository.TestOrderRepository;
import com.example.test_order_service.repository.TestResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TestResultServiceImplTest {

    @Mock TestResultRepository resultRepository;
    @Mock TestOrderRepository orderRepository;
    @Mock TestResultMapper mapper;

    @InjectMocks TestResultServiceImpl service;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
    }

    private TestOrder validOrder(boolean deleted) {
        TestOrder o = new TestOrder();
        o.setTestOrderId("to1");
        o.setPatientName("A");
        o.setCitizenId("123");
        o.setCountry("VN");
        o.setBloodCollectionId("BC1");
        o.setStatus(TestOrderStatus.PENDING);
        o.setDeleted(deleted);
        return o;
    }

    // -------- getResultByBloodCollectionId --------

    @Test
    void getResultByBloodCollectionId_notFound_shouldThrow() {
        when(resultRepository.findByBloodCollectionId("BC1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.getResultByBloodCollectionId("BC1"));
    }

    @Test
    void getResultByBloodCollectionId_success() {
        TestResult r = new TestResult();
        r.setResultId("r1");
        r.setBloodCollectionId("BC1");
        r.setHl7RawData("MSH|...");

        when(resultRepository.findByBloodCollectionId("BC1")).thenReturn(Optional.of(r));
        when(mapper.toTestResultResponse(r))
                .thenReturn(TestResultResponse.builder().bloodCollectionId("BC1").build());

        RestResponse<?> res = service.getResultByBloodCollectionId("BC1");

        assertEquals(200, res.getStatusCode());
        assertTrue(res.getMessage().toString().contains("retrivived"));
    }

    // -------- receiveHl7 validation --------

    @Test
    void receiveHl7_blank_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.receiveHl7("   "));
    }

    @Test
    void receiveHl7_notStartWithMSH_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.receiveHl7("OBR|x\nOBX|x"));
    }

    @Test
    void receiveHl7_missingSeparators_shouldThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> service.receiveHl7("MSH|^~\\&|INST|NOSEP"));
    }

    @Test
    void receiveHl7_missingOBR_shouldThrow() {
        String hl7 = """
                MSH|^~\\&|INST|x
                OBX|1|NM|WBC^White Blood Cell||5.0|mmol/L
                """;
        assertThrows(IllegalArgumentException.class,
                () -> service.receiveHl7(hl7));
    }

    @Test
    void receiveHl7_missingOBX_shouldThrow() {
        String hl7 = """
                MSH|^~\\&|INST|x
                OBR|1|BC1
                """;
        assertThrows(IllegalArgumentException.class,
                () -> service.receiveHl7(hl7));
    }

    @Test
    void receiveHl7_missingBloodCollectionInOBR_shouldThrow() {
        String hl7 = """
                MSH|^~\\&|INST|x
                OBR|1|
                OBX|1|NM|WBC^White Blood Cell||5.0|mmol/L
                """;
        assertThrows(IllegalArgumentException.class,
                () -> service.receiveHl7(hl7));
    }

    @Test
    void receiveHl7_duplicateResult_shouldThrow() {
        String hl7 = """
                MSH|^~\\&|INST|x
                OBR|1|BC1
                OBX|1|NM|WBC^White Blood Cell||5.0|mmol/L
                """;

        when(resultRepository.findByBloodCollectionId("BC1"))
                .thenReturn(Optional.of(new TestResult()));

        assertThrows(IllegalArgumentException.class,
                () -> service.receiveHl7(hl7));
    }

    @Test
    void receiveHl7_orderNotFound_shouldThrow() {
        String hl7 = """
                MSH|^~\\&|INST|x
                OBR|1|BC1
                OBX|1|NM|WBC^White Blood Cell||5.0|mmol/L
                """;

        when(resultRepository.findByBloodCollectionId("BC1")).thenReturn(Optional.empty());
        when(orderRepository.findByBloodCollectionId("BC1")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.receiveHl7(hl7));
    }

    @Test
    void receiveHl7_orderDeleted_shouldThrow() {
        String hl7 = """
                MSH|^~\\&|INST|x
                OBR|1|BC1
                OBX|1|NM|WBC^White Blood Cell||5.0|mmol/L
                """;

        when(resultRepository.findByBloodCollectionId("BC1")).thenReturn(Optional.empty());
        when(orderRepository.findByBloodCollectionId("BC1"))
                .thenReturn(Optional.of(validOrder(true)));

        assertThrows(ResourceNotFoundException.class,
                () -> service.receiveHl7(hl7));
    }

    @Test
    void receiveHl7_success_shouldSaveResultAndUpdateOrderStatus() {
        String hl7 = """
                MSH|^~\\&|INST|x
                OBR|1|BC1
                OBX|1|NM|WBC^White Blood Cell||5.0|mmol/L|3.5-6.5|N
                OBX|2|NM|RBC^Red Blood Cell||4.5|mmol/L
                """;

        TestOrder order = validOrder(false);

        when(resultRepository.findByBloodCollectionId("BC1")).thenReturn(Optional.empty());
        when(orderRepository.findByBloodCollectionId("BC1")).thenReturn(Optional.of(order));
        when(resultRepository.save(any(TestResult.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(TestOrder.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(mapper.toTestResultResponse(any(TestResult.class)))
                .thenReturn(TestResultResponse.builder().bloodCollectionId("BC1").build());

        RestResponse<TestResultResponse> res = service.receiveHl7(hl7);

        assertEquals(200, res.getStatusCode());
        assertEquals("HL7 data processed successfully", res.getMessage());
        assertEquals(TestOrderStatus.COMPLETED, order.getStatus());

        verify(resultRepository).save(any(TestResult.class));
        verify(orderRepository).save(order);
    }

    // -------- republishTestResultEvent --------

    @Test
    void republishTestResultEvent_shouldReturn501() {
        RestResponse<Void> res = service.republishTestResultEvent("to1");
        assertEquals(501, res.getStatusCode());
        assertEquals("Not Implemented", res.getError());
    }
}

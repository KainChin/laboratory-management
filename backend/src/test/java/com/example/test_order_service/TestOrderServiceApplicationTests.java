package com.example.test_order_service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TestOrderServiceApplicationTests {

    @Test
    void constructor_coverage() {
        TestOrderServiceApplication app = new TestOrderServiceApplication();
        assertNotNull(app);
    }

    @Test
    void main_shouldSetDefaultTimezoneAndRunSpringApplication() {
        TimeZone original = TimeZone.getDefault();

        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {

            mocked.when(() -> SpringApplication.run(
                            Mockito.eq(TestOrderServiceApplication.class),
                            Mockito.any(String[].class)))
                    .thenReturn(Mockito.mock(ConfigurableApplicationContext.class));

            // act
            TestOrderServiceApplication.main(new String[]{});

            // assert
            assertEquals("Asia/Ho_Chi_Minh", TimeZone.getDefault().getID());

            mocked.verify(() -> SpringApplication.run(
                    Mockito.eq(TestOrderServiceApplication.class),
                    Mockito.any(String[].class)));

        } finally {
            TimeZone.setDefault(original);
        }
    }
}

package com.example.test_order_service;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TestOrderServiceApplicationTests {

    @Test
    void main_shouldSetDefaultTimezoneAndRunSpringApplication() {
        TimeZone original = TimeZone.getDefault();

        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {

            mocked.when(() -> SpringApplication.run(
                            TestOrderServiceApplication.class,
                            new String[]{}))
                    .thenReturn(Mockito.mock(ConfigurableApplicationContext.class));

            // act
            TestOrderServiceApplication.main(new String[]{});

            // assert
            assertEquals("Asia/Ho_Chi_Minh", TimeZone.getDefault().getID());

            mocked.verify(() -> SpringApplication.run(
                    TestOrderServiceApplication.class,
                    new String[]{}));

        } finally {
            TimeZone.setDefault(original);
        }
    }
}

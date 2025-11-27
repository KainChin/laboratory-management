package com.example.test_order_service.repository;

import com.example.test_order_service.entity.FlaggingConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FlaggingConfigRepositoryTest {

    @Autowired
    private FlaggingConfigRepository repository;

    @Test
    void findByParameterIgnoreCase_shouldFindRegardlessCase() {
        FlaggingConfig cfg = new FlaggingConfig();
        cfg.setConfigId("cfg1");
        cfg.setParameter("GlU");
        cfg.setMinValue(3.5);
        cfg.setMaxValue(6.5);
        cfg.setUnit("mmol/L");
        repository.save(cfg);

        Optional<FlaggingConfig> found = repository.findByParameterIgnoreCase("glu");

        assertTrue(found.isPresent());
        assertEquals("GlU", found.get().getParameter());
    }

    @Test
    void findByParameterIgnoreCase_shouldEmptyWhenNotFound() {
        assertTrue(repository.findByParameterIgnoreCase("xxx").isEmpty());
    }
}

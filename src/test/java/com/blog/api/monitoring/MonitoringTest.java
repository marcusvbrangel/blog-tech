package com.blog.api.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class MonitoringTest {

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private Counter postCreationCounter;

    @Autowired
    private Counter userRegistrationCounter;

    @Autowired
    private Counter commentCreationCounter;

    @Autowired
    private Timer databaseQueryTimer;

    @Test
    void testMeterRegistryIsConfigured() {
        assertNotNull(meterRegistry);
        assertTrue(meterRegistry.getMeters().size() > 0);
    }

    @Test
    void testCustomCountersAreRegistered() {
        assertNotNull(postCreationCounter);
        assertNotNull(userRegistrationCounter);
        assertNotNull(commentCreationCounter);
        
        // Test that counters can be incremented
        double initialPostCount = postCreationCounter.count();
        postCreationCounter.increment();
        assertTrue(postCreationCounter.count() > initialPostCount);
    }

    @Test
    void testCustomTimersAreRegistered() {
        assertNotNull(databaseQueryTimer);
        
        // Test that timer exists and can be used
        databaseQueryTimer.record(() -> {
            try {
                Thread.sleep(10); // Simulate some work
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        
        assertTrue(databaseQueryTimer.count() > 0);
    }

    @Test
    void testPrometheusMetricsEndpoint() {
        // This test would require web testing setup
        // For now, we just verify the metrics are present
        boolean hasPostMetrics = meterRegistry.getMeters().stream()
                .anyMatch(meter -> meter.getId().getName().startsWith("blog_api_posts"));
        
        boolean hasUserMetrics = meterRegistry.getMeters().stream()
                .anyMatch(meter -> meter.getId().getName().startsWith("blog_api_users"));
        
        assertTrue(hasPostMetrics || hasUserMetrics);
    }
}
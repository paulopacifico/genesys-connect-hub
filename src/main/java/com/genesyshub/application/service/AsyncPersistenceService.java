package com.genesyshub.application.service;

import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.port.out.MetricsPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncPersistenceService {

    private final MetricsPersistencePort metricsPersistencePort;

    @Async("metricsTaskExecutor")
    public void persistMetricsAsync(List<ConversationMetric> metrics) {
        try {
            metricsPersistencePort.saveMetrics(metrics);
        } catch (Exception e) {
            log.error("Async metrics persistence failed for {} metrics: {}", metrics.size(), e.getMessage(), e);
            // re-throw so AsyncUncaughtExceptionHandler can handle it
            throw new RuntimeException("Async metrics persistence failed", e);
        }
    }
}

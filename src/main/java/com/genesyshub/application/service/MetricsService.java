package com.genesyshub.application.service;

import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.model.ConversationMetricSummary;
import com.genesyshub.domain.port.in.MetricsUseCase;
import com.genesyshub.domain.port.out.ConversationMetricPort;
import com.genesyshub.domain.port.out.MetricsPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsService implements MetricsUseCase {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ConversationMetricPort conversationMetricPort;
    private final MetricsPersistencePort metricsPersistencePort;

    @Override
    public List<ConversationMetric> getMetricsByQueue(String queueId, Instant from, Instant to) {
        log.debug("Fetching metrics for queueId={}, from={}, to={}", queueId, from, to);
        List<ConversationMetric> metrics = conversationMetricPort.fetchConversationMetrics(queueId, from, to);
        persistAsync(metrics);
        return metrics;
    }

    @Override
    public ConversationMetricSummary getSummaryByQueue(String queueId, Instant from, Instant to) {
        List<ConversationMetric> metrics = getMetricsByQueue(queueId, from, to);

        long total = metrics.size();
        long abandoned = metrics.stream().filter(ConversationMetric::abandoned).count();
        double avgHandleTime = metrics.stream()
                .mapToLong(ConversationMetric::handleTimeSeconds)
                .average()
                .orElse(0.0);
        double abandonRate = total > 0 ? (double) abandoned / total * 100.0 : 0.0;

        String period = formatPeriod(from, to);

        log.info("Summary for queueId={}: total={}, abandoned={}, abandonRate={}%, avgHandle={}s",
                queueId, total, abandoned, String.format("%.2f", abandonRate), String.format("%.1f", avgHandleTime));

        return new ConversationMetricSummary(total, avgHandleTime, abandoned, abandonRate, period);
    }

    @Override
    public List<ConversationMetric> getAbandonedCalls(Instant from, Instant to) {
        log.debug("Fetching abandoned calls from={}, to={}", from, to);
        return metricsPersistencePort.findAbandonedByPeriod(from, to);
    }

    // -------------------------------------------------------------------------

    @Async
    protected void persistAsync(List<ConversationMetric> metrics) {
        try {
            metricsPersistencePort.saveMetrics(metrics);
        } catch (Exception e) {
            log.warn("Async metrics persistence failed: {}", e.getMessage());
        }
    }

    private String formatPeriod(Instant from, Instant to) {
        LocalDate start = from.atOffset(ZoneOffset.UTC).toLocalDate();
        LocalDate end = to.atOffset(ZoneOffset.UTC).toLocalDate();
        return start.format(DATE_FMT) + " to " + end.format(DATE_FMT);
    }
}

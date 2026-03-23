package com.genesyshub.application.service;

import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.model.ConversationMetricSummary;
import com.genesyshub.domain.port.out.ConversationMetricPort;
import com.genesyshub.domain.port.out.MetricsPersistencePort;
import com.genesyshub.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    private ConversationMetricPort conversationMetricPort;

    @Mock
    private MetricsPersistencePort metricsPersistencePort;

    @InjectMocks
    private MetricsService metricsService;

    @Captor
    private ArgumentCaptor<List<ConversationMetric>> metricsCaptor;

    private final Instant from = TestFixtures.from();
    private final Instant to = TestFixtures.to();

    @Test
    void getMetricsByQueue_fetchesFromGenesysAndPersists() {
        List<ConversationMetric> metrics = List.of(
                TestFixtures.createConversationMetric("c1", false, 200L),
                TestFixtures.createConversationMetric("c2", true, 100L)
        );
        when(conversationMetricPort.fetchConversationMetrics("queue-001", from, to)).thenReturn(metrics);

        List<ConversationMetric> result = metricsService.getMetricsByQueue("queue-001", from, to);

        assertThat(result).hasSize(2).containsExactlyElementsOf(metrics);

        // verify async persist was triggered with the same data
        verify(metricsPersistencePort).saveMetrics(metricsCaptor.capture());
        assertThat(metricsCaptor.getValue())
                .hasSize(2)
                .extracting(ConversationMetric::conversationId)
                .containsExactlyInAnyOrder("c1", "c2");
    }

    @Test
    void getMetricsByQueue_returnsEmptyList_whenNoData() {
        when(conversationMetricPort.fetchConversationMetrics("queue-001", from, to)).thenReturn(List.of());

        List<ConversationMetric> result = metricsService.getMetricsByQueue("queue-001", from, to);

        assertThat(result).isEmpty();
        verify(metricsPersistencePort).saveMetrics(List.of());
    }

    @Test
    void getSummaryByQueue_calculatesAbandonRateCorrectly() {
        List<ConversationMetric> metrics = List.of(
                TestFixtures.createConversationMetric("c1", false, 300L),
                TestFixtures.createConversationMetric("c2", false, 500L),
                TestFixtures.createConversationMetric("c3", true, 60L),
                TestFixtures.createConversationMetric("c4", true, 30L)
        );
        when(conversationMetricPort.fetchConversationMetrics("queue-001", from, to)).thenReturn(metrics);

        ConversationMetricSummary summary = metricsService.getSummaryByQueue("queue-001", from, to);

        assertThat(summary.totalConversations()).isEqualTo(4L);
        assertThat(summary.abandonedCount()).isEqualTo(2L);
        assertThat(summary.abandonRate()).isCloseTo(50.0, within(0.001));
        assertThat(summary.averageHandleTimeSeconds()).isCloseTo(222.5, within(0.001));
        assertThat(summary.period()).contains("2024-01-01").contains("2024-01-31");
    }

    @Test
    void getSummaryByQueue_returnsZeroAbandonRate_whenNoAbandonedCalls() {
        List<ConversationMetric> metrics = List.of(
                TestFixtures.createConversationMetric("c1", false, 300L),
                TestFixtures.createConversationMetric("c2", false, 600L)
        );
        when(conversationMetricPort.fetchConversationMetrics("queue-001", from, to)).thenReturn(metrics);

        ConversationMetricSummary summary = metricsService.getSummaryByQueue("queue-001", from, to);

        assertThat(summary.abandonedCount()).isZero();
        assertThat(summary.abandonRate()).isZero();
        assertThat(summary.totalConversations()).isEqualTo(2L);
        assertThat(summary.averageHandleTimeSeconds()).isCloseTo(450.0, within(0.001));
    }

    @Test
    void getSummaryByQueue_returnsZeros_whenNoConversations() {
        when(conversationMetricPort.fetchConversationMetrics("queue-001", from, to)).thenReturn(List.of());

        ConversationMetricSummary summary = metricsService.getSummaryByQueue("queue-001", from, to);

        assertThat(summary.totalConversations()).isZero();
        assertThat(summary.abandonedCount()).isZero();
        assertThat(summary.abandonRate()).isZero();
        assertThat(summary.averageHandleTimeSeconds()).isZero();
    }

    @Test
    void getAbandonedCalls_delegatesToPersistence() {
        List<ConversationMetric> abandoned = List.of(
                TestFixtures.createConversationMetric("c1", true, 30L)
        );
        when(metricsPersistencePort.findAbandonedByPeriod(from, to)).thenReturn(abandoned);

        List<ConversationMetric> result = metricsService.getAbandonedCalls(from, to);

        assertThat(result).hasSize(1).allMatch(ConversationMetric::abandoned);
        verify(metricsPersistencePort).findAbandonedByPeriod(from, to);
    }
}

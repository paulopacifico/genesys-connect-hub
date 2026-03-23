package com.genesyshub.integration;

import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.port.out.MetricsPersistencePort;
import com.genesyshub.infrastructure.persistence.repository.ConversationMetricRepository;
import com.genesyshub.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "classpath:test-data.sql",   executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:cleanup.sql",     executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class MetricsPersistenceAdapterIT extends AbstractIntegrationTest {

    @Autowired
    private MetricsPersistencePort metricsPersistencePort;

    @Autowired
    private ConversationMetricRepository repository;

    @Test
    void saveMetrics_persistsAllMetrics_withoutDuplicates() {
        List<ConversationMetric> metrics = List.of(
                TestFixtures.createConversationMetric("new-conv-1", false, 200L),
                TestFixtures.createConversationMetric("new-conv-2", false, 300L),
                TestFixtures.createConversationMetric("new-conv-3", true,  60L),
                TestFixtures.createConversationMetric("new-conv-4", false, 400L),
                TestFixtures.createConversationMetric("new-conv-5", false, 500L)
        );

        metricsPersistencePort.saveMetrics(metrics);
        long countAfterFirst = repository.count();

        // call again with the same IDs — should not create duplicates
        metricsPersistencePort.saveMetrics(metrics);
        long countAfterSecond = repository.count();

        // test-data.sql seeds 5 rows; we added 5 more
        assertThat(countAfterFirst).isEqualTo(countAfterSecond);
        assertThat(repository.existsByConversationId("new-conv-1")).isTrue();
        assertThat(repository.existsByConversationId("new-conv-5")).isTrue();
    }

    @Test
    void findByQueueAndPeriod_returnsOnlyMetricsInRange() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to   = Instant.parse("2024-01-31T23:59:59Z");

        List<ConversationMetric> results =
                metricsPersistencePort.findByQueueAndPeriod("queue-001", from, to);

        // conv-in-range-1,2,3 are in range for queue-001; conv-out-of-range is Feb; conv-other-queue is queue-002
        assertThat(results).hasSize(3)
                .extracting(ConversationMetric::conversationId)
                .containsExactlyInAnyOrder("conv-in-range-1", "conv-in-range-2", "conv-in-range-3");
    }

    @Test
    void findByQueueAndPeriod_returnsEmpty_whenNoMatchingQueue() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to   = Instant.parse("2024-01-31T23:59:59Z");

        List<ConversationMetric> results =
                metricsPersistencePort.findByQueueAndPeriod("queue-999", from, to);

        assertThat(results).isEmpty();
    }

    @Test
    void findAbandonedByPeriod_returnsOnlyAbandonedMetrics() {
        Instant from = Instant.parse("2024-01-01T00:00:00Z");
        Instant to   = Instant.parse("2024-01-31T23:59:59Z");

        List<ConversationMetric> abandoned =
                metricsPersistencePort.findAbandonedByPeriod(from, to);

        // conv-in-range-2 (queue-001, abandoned) and conv-other-queue (queue-002, abandoned) are in January
        assertThat(abandoned).isNotEmpty()
                .allMatch(ConversationMetric::abandoned);
        assertThat(abandoned).extracting(ConversationMetric::conversationId)
                .contains("conv-in-range-2", "conv-other-queue");
    }
}

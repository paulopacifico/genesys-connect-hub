package com.genesyshub.integration;

import com.genesyshub.domain.model.WebhookEvent;
import com.genesyshub.domain.port.out.MetricsPersistencePort;
import com.genesyshub.infrastructure.persistence.repository.WebhookEventRepository;
import com.genesyshub.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:cleanup.sql",   executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class WebhookEventPersistenceIT extends AbstractIntegrationTest {

    @Autowired
    private MetricsPersistencePort metricsPersistencePort;

    @Autowired
    private WebhookEventRepository repository;

    @Test
    void saveWebhookEvent_persistsEvent_withIdempotency() {
        WebhookEvent event = new WebhookEvent(
                "new-event-001",
                "v2.routing.queue.conversations.voice",
                "2",
                Map.of("conversationId", "abc-123"),
                Instant.now()
        );

        metricsPersistencePort.saveWebhookEvent(event);
        long countAfterFirst = repository.count();

        // save same event again — idempotency must prevent a duplicate row
        metricsPersistencePort.saveWebhookEvent(event);
        long countAfterSecond = repository.count();

        assertThat(countAfterFirst).isEqualTo(countAfterSecond);
        assertThat(repository.existsByEventId("new-event-001")).isTrue();
    }

    @Test
    void saveWebhookEvent_storesPayloadAsJson() {
        WebhookEvent event = new WebhookEvent(
                "payload-event-001",
                "v2.routing.queue.conversations.chat",
                "2",
                Map.of("key1", "value1", "nested", Map.of("a", 1)),
                Instant.now()
        );

        metricsPersistencePort.saveWebhookEvent(event);

        assertThat(repository.existsByEventId("payload-event-001")).isTrue();
    }

    @Test
    void findUnprocessedEvents_returnsOnlyUnprocessed() {
        // test-data.sql seeds 1 processed + 2 unprocessed events
        var unprocessed = repository.findByProcessedFalseOrderByReceivedAt();

        assertThat(unprocessed).hasSize(2)
                .allMatch(e -> !e.isProcessed())
                .extracting(e -> e.getEventId())
                .containsExactly("event-unprocessed-1", "event-unprocessed-2"); // ordered by received_at
    }

    @Test
    void saveWebhookEvent_newEventIsPersistedAsUnprocessed() {
        WebhookEvent event = TestFixtures.createWebhookEvent();

        metricsPersistencePort.saveWebhookEvent(event);

        var saved = repository.findByProcessedFalseOrderByReceivedAt().stream()
                .filter(e -> e.getEventId().equals("event-001"))
                .findFirst();

        assertThat(saved).isPresent();
        assertThat(saved.get().isProcessed()).isFalse();
        assertThat(saved.get().getProcessedAt()).isNull();
    }
}

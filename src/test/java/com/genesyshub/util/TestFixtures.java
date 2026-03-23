package com.genesyshub.util;

import com.genesyshub.domain.model.Agent;
import com.genesyshub.domain.model.AgentStatus;
import com.genesyshub.domain.model.ApiHealthStatus;
import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.model.ConversationMetricSummary;
import com.genesyshub.domain.model.Queue;
import com.genesyshub.domain.model.WebhookEvent;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public final class TestFixtures {

    private TestFixtures() {}

    public static Queue createQueue() {
        return new Queue(
                "queue-001",
                "Support Queue",
                "Division A",
                List.of("voice", "chat"),
                5,
                3,
                2,
                Instant.parse("2024-01-01T00:00:00Z")
        );
    }

    public static Queue createQueue(String id, String name, List<String> mediaTypes) {
        return new Queue(id, name, "Division A", mediaTypes, 0, 0, 0, Instant.now());
    }

    public static Agent createAgent() {
        return new Agent(
                "agent-001",
                "John Doe",
                "john.doe@example.com",
                AgentStatus.ON_QUEUE,
                "Division A",
                List.of("queue-001"),
                Instant.parse("2024-01-01T10:00:00Z")
        );
    }

    public static Agent createAgent(String id, AgentStatus status) {
        return new Agent(id, "Agent " + id, id + "@example.com", status,
                "Division A", List.of("queue-001"), Instant.now());
    }

    public static ConversationMetric createConversationMetric() {
        return new ConversationMetric(
                "conv-001",
                "queue-001",
                "agent-001",
                "voice",
                "inbound",
                Instant.parse("2024-01-01T09:00:00Z"),
                Instant.parse("2024-01-01T09:05:00Z"),
                300L,
                false
        );
    }

    public static ConversationMetric createConversationMetric(String id, boolean abandoned, long handleSeconds) {
        return new ConversationMetric(
                id, "queue-001", "agent-001", "voice", "inbound",
                Instant.parse("2024-01-01T09:00:00Z"),
                Instant.parse("2024-01-01T09:05:00Z"),
                handleSeconds,
                abandoned
        );
    }

    public static WebhookEvent createWebhookEvent() {
        return new WebhookEvent(
                "event-001",
                "v2.routing.queue.conversations.voice",
                "2",
                Map.of("key", "value"),
                Instant.parse("2024-01-01T10:00:00Z")
        );
    }

    public static ApiHealthStatus createHealthStatus(boolean connected) {
        return new ApiHealthStatus(connected, "mypurecloud.com", "Acme Corp", Instant.now());
    }

    public static ConversationMetricSummary createSummary() {
        return new ConversationMetricSummary(10L, 250.0, 2L, 20.0, "2024-01-01 to 2024-01-31");
    }

    public static Instant from() {
        return Instant.parse("2024-01-01T00:00:00Z");
    }

    public static Instant to() {
        return Instant.parse("2024-01-31T23:59:59Z");
    }
}

package com.genesyshub.domain.model;

import java.time.Instant;
import java.util.Map;

public record WebhookEvent(
        String eventId,
        String topicName,
        String version,
        Map<String, Object> payload,
        Instant receivedAt
) {}

package com.genesyshub.domain.model;

import java.time.Instant;

public record ConversationMetric(
        String conversationId,
        String queueId,
        String agentId,
        String mediaType,
        String direction,
        Instant startTime,
        Instant endTime,
        long handleTimeSeconds,
        boolean abandoned
) {}

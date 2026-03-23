package com.genesyshub.infrastructure.web.dto;

import java.time.Instant;

public record ConversationMetricResponse(
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

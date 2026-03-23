package com.genesyshub.domain.model;

import java.time.Instant;
import java.util.List;

public record Queue(
        String id,
        String name,
        String division,
        List<String> mediaTypes,
        int activeAgents,
        int onQueueAgents,
        int offQueueAgents,
        Instant createdAt
) {}

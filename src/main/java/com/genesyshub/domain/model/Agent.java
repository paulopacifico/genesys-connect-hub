package com.genesyshub.domain.model;

import java.time.Instant;
import java.util.List;

public record Agent(
        String id,
        String name,
        String email,
        AgentStatus status,
        String division,
        List<String> queueIds,
        Instant lastUpdated
) {}

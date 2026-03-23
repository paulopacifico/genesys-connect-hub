package com.genesyshub.infrastructure.web.dto;

import java.util.List;

public record QueueResponse(
        String id,
        String name,
        String division,
        List<String> mediaTypes,
        int activeAgents,
        int onQueueAgents
) {}

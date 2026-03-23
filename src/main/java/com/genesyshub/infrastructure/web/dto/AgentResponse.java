package com.genesyshub.infrastructure.web.dto;

import java.util.List;

public record AgentResponse(
        String id,
        String name,
        String email,
        String status,
        String division,
        List<String> queueIds
) {}

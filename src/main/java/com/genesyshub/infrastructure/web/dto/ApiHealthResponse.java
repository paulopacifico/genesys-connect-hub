package com.genesyshub.infrastructure.web.dto;

import java.time.Instant;

public record ApiHealthResponse(
        boolean connected,
        String region,
        String organizationName,
        Instant checkedAt
) {}

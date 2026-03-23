package com.genesyshub.domain.model;

import java.time.Instant;

public record ApiHealthStatus(
        boolean connected,
        String region,
        String organizationName,
        Instant checkedAt
) {}

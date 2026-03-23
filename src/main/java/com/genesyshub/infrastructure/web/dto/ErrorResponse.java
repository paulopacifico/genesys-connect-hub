package com.genesyshub.infrastructure.web.dto;

import java.time.Instant;

public record ErrorResponse(
        String errorCode,
        String message,
        Instant timestamp,
        String path
) {}

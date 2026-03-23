package com.genesyshub.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record WebhookEventRequest(
        String eventId,          // optional; Genesys sends this in their webhook envelope
        @NotBlank String topicName,
        String version,
        Map<String, Object> payload
) {}

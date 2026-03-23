package com.genesyshub.infrastructure.web.dto;

public record ConversationSummaryResponse(
        long totalConversations,
        double averageHandleTimeSeconds,
        long abandonedCount,
        double abandonRate,
        String period
) {}

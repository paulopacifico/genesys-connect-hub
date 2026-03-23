package com.genesyshub.domain.model;

public record ConversationMetricSummary(
        long totalConversations,
        double averageHandleTimeSeconds,
        long abandonedCount,
        double abandonRate,
        String period
) {}

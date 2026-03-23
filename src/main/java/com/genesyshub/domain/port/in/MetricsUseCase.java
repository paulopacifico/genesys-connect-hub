package com.genesyshub.domain.port.in;

import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.model.ConversationMetricSummary;

import java.time.Instant;
import java.util.List;

public interface MetricsUseCase {

    List<ConversationMetric> getMetricsByQueue(String queueId, Instant from, Instant to);

    ConversationMetricSummary getSummaryByQueue(String queueId, Instant from, Instant to);

    List<ConversationMetric> getAbandonedCalls(Instant from, Instant to);
}

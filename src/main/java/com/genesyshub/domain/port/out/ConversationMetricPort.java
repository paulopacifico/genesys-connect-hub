package com.genesyshub.domain.port.out;

import com.genesyshub.domain.model.ConversationMetric;

import java.time.Instant;
import java.util.List;

public interface ConversationMetricPort {

    List<ConversationMetric> fetchConversationMetrics(String queueId, Instant from, Instant to);
}

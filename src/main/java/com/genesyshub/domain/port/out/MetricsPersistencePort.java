package com.genesyshub.domain.port.out;

import com.genesyshub.domain.model.ConversationMetric;

import java.time.Instant;
import java.util.List;

public interface MetricsPersistencePort {

    void saveMetrics(List<ConversationMetric> metrics);

    List<ConversationMetric> findByQueueAndPeriod(String queueId, Instant from, Instant to);

    List<ConversationMetric> findAbandonedByPeriod(Instant from, Instant to);
}

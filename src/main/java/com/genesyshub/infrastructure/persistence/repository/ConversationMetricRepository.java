package com.genesyshub.infrastructure.persistence.repository;

import com.genesyshub.infrastructure.persistence.entity.ConversationMetricEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface ConversationMetricRepository extends JpaRepository<ConversationMetricEntity, Long> {

    List<ConversationMetricEntity> findByQueueIdAndStartTimeBetween(
            String queueId, Instant from, Instant to);

    List<ConversationMetricEntity> findByAbandonedTrueAndStartTimeBetween(
            Instant from, Instant to);

    boolean existsByConversationId(String conversationId);
}

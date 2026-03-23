package com.genesyshub.infrastructure.persistence;

import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.model.WebhookEvent;
import com.genesyshub.domain.port.out.MetricsPersistencePort;
import com.genesyshub.domain.port.out.WebhookPersistencePort;
import com.genesyshub.infrastructure.persistence.entity.ConversationMetricEntity;
import com.genesyshub.infrastructure.persistence.mapper.ConversationMetricMapper;
import com.genesyshub.infrastructure.persistence.mapper.WebhookEventMapper;
import com.genesyshub.infrastructure.persistence.repository.ConversationMetricRepository;
import com.genesyshub.infrastructure.persistence.repository.WebhookEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsPersistenceAdapter implements MetricsPersistencePort, WebhookPersistencePort {

    private final ConversationMetricRepository conversationMetricRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final ConversationMetricMapper conversationMetricMapper;
    private final WebhookEventMapper webhookEventMapper;

    @Override
    @Transactional
    public void saveMetrics(List<ConversationMetric> metrics) {
        if (metrics == null || metrics.isEmpty()) return;

        List<ConversationMetricEntity> toSave = metrics.stream()
                .filter(m -> !conversationMetricRepository.existsByConversationId(m.conversationId()))
                .map(conversationMetricMapper::toEntity)
                .toList();

        if (toSave.isEmpty()) {
            log.debug("All {} metrics already persisted, skipping", metrics.size());
            return;
        }

        conversationMetricRepository.saveAll(toSave);
        log.info("Saved {}/{} conversation metrics (duplicates skipped)",
                toSave.size(), metrics.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationMetric> findByQueueAndPeriod(String queueId, Instant from, Instant to) {
        log.debug("Querying metrics: queueId={}, from={}, to={}", queueId, from, to);
        List<ConversationMetricEntity> entities =
                conversationMetricRepository.findByQueueIdAndStartTimeBetween(queueId, from, to);
        return conversationMetricMapper.toDomainList(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationMetric> findAbandonedByPeriod(Instant from, Instant to) {
        log.debug("Querying abandoned metrics: from={}, to={}", from, to);
        List<ConversationMetricEntity> entities =
                conversationMetricRepository.findByAbandonedTrueAndStartTimeBetween(from, to);
        return conversationMetricMapper.toDomainList(entities);
    }

    @Override
    @Transactional
    public void saveWebhookEvent(WebhookEvent event) {
        if (webhookEventRepository.existsByEventId(event.eventId())) {
            log.debug("Webhook event already persisted (idempotent): eventId={}", event.eventId());
            return;
        }
        webhookEventRepository.save(webhookEventMapper.toEntity(event));
        log.info("Saved webhook event: eventId={}, topic={}", event.eventId(), event.topicName());
    }
}

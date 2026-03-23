package com.genesyshub.infrastructure.persistence.repository;

import com.genesyshub.infrastructure.persistence.entity.WebhookEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookEventRepository extends JpaRepository<WebhookEventEntity, Long> {

    List<WebhookEventEntity> findByProcessedFalseOrderByReceivedAt();

    boolean existsByEventId(String eventId);
}

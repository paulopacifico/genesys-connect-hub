package com.genesyshub.application.service;

import com.genesyshub.domain.model.WebhookEvent;
import com.genesyshub.domain.port.in.WebhookUseCase;
import com.genesyshub.domain.port.out.WebhookPersistencePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService implements WebhookUseCase {

    private final WebhookPersistencePort metricsPersistencePort;

    @Override
    @Transactional
    public void processEvent(WebhookEvent event) {
        log.info("Processing webhook event: eventId={}, topic={}", event.eventId(), event.topicName());
        metricsPersistencePort.saveWebhookEvent(event);
        log.debug("Webhook event persisted (idempotent): eventId={}", event.eventId());
    }
}

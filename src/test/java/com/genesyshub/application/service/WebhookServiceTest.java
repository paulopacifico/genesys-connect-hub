package com.genesyshub.application.service;

import com.genesyshub.domain.model.WebhookEvent;
import com.genesyshub.domain.port.out.MetricsPersistencePort;
import com.genesyshub.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    private MetricsPersistencePort metricsPersistencePort;

    @InjectMocks
    private WebhookService webhookService;

    @Test
    void processEvent_savesEventToPersistence() {
        WebhookEvent event = TestFixtures.createWebhookEvent();

        webhookService.processEvent(event);

        ArgumentCaptor<WebhookEvent> captor = ArgumentCaptor.forClass(WebhookEvent.class);
        verify(metricsPersistencePort).saveWebhookEvent(captor.capture());
        assertThat(captor.getValue().eventId()).isEqualTo("event-001");
        assertThat(captor.getValue().topicName()).isEqualTo("v2.routing.queue.conversations.voice");
    }

    @Test
    void processEvent_delegatesIdempotencyToPersistenceLayer() {
        WebhookEvent event = TestFixtures.createWebhookEvent();

        webhookService.processEvent(event);
        webhookService.processEvent(event);

        // Both calls forwarded — idempotency is enforced in the adapter, not the service
        verify(metricsPersistencePort, org.mockito.Mockito.times(2)).saveWebhookEvent(event);
    }
}

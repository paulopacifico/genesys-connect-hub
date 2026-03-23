package com.genesyshub.domain.port.out;

import com.genesyshub.domain.model.WebhookEvent;

public interface WebhookPersistencePort {
    void saveWebhookEvent(WebhookEvent event);
}

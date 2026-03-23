package com.genesyshub.domain.port.in;

import com.genesyshub.domain.model.WebhookEvent;

public interface WebhookUseCase {

    void processEvent(WebhookEvent event);
}

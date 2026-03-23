package com.genesyshub.infrastructure.web.controller;

import com.genesyshub.domain.model.WebhookEvent;
import com.genesyshub.domain.port.in.WebhookUseCase;
import com.genesyshub.infrastructure.web.dto.WebhookEventRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Genesys Cloud webhook event receiver")
public class WebhookController {

    private final WebhookUseCase webhookUseCase;

    @PostMapping("/genesys")
    @Operation(summary = "Receive a Genesys Cloud webhook event")
    @ApiResponse(responseCode = "202", description = "Event accepted for processing")
    @ApiResponse(responseCode = "400", description = "Invalid request body")
    public ResponseEntity<Void> receiveEvent(@Valid @RequestBody WebhookEventRequest request) {
        WebhookEvent event = new WebhookEvent(
                UUID.randomUUID().toString(),
                request.topicName(),
                request.version(),
                request.payload(),
                Instant.now()
        );
        webhookUseCase.processEvent(event);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}

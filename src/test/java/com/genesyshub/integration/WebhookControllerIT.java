package com.genesyshub.integration;

import com.genesyshub.domain.port.in.WebhookUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class WebhookControllerIT extends AbstractIntegrationTest {

    @MockBean
    private WebhookUseCase webhookUseCase;

    @Test
    void receiveEvent_returns202_withValidBody() {
        String body = """
                {
                    "topicName": "v2.routing.queue.conversations.voice",
                    "version": "2",
                    "payload": { "conversationId": "abc-123" }
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/v1/webhooks/genesys",
                new HttpEntity<>(body, headers),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void receiveEvent_returns202_withNullPayload() {
        String body = """
                {
                    "topicName": "v2.routing.queue.conversations.voice",
                    "version": "2"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/v1/webhooks/genesys",
                new HttpEntity<>(body, headers),
                Void.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void receiveEvent_returns400_whenTopicNameMissing() {
        String body = """
                {
                    "version": "2",
                    "payload": { "key": "value" }
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/webhooks/genesys",
                new HttpEntity<>(body, headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void receiveEvent_returns400_whenBodyIsEmpty() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/v1/webhooks/genesys",
                new HttpEntity<>("{}", headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}

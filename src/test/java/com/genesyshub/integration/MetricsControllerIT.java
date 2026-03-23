package com.genesyshub.integration;

import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.model.ConversationMetricSummary;
import com.genesyshub.domain.port.in.MetricsUseCase;
import com.genesyshub.infrastructure.web.dto.ConversationMetricResponse;
import com.genesyshub.infrastructure.web.dto.ConversationSummaryResponse;
import com.genesyshub.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MetricsControllerIT extends AbstractIntegrationTest {

    @MockBean
    private MetricsUseCase metricsUseCase;

    private static final String FROM = "2024-01-01T00:00:00Z";
    private static final String TO   = "2024-01-31T23:59:59Z";

    @Test
    void getMetricsByQueue_returns200_withMetrics() {
        List<ConversationMetric> metrics = List.of(
                TestFixtures.createConversationMetric("c1", false, 300L),
                TestFixtures.createConversationMetric("c2", true,  60L)
        );
        when(metricsUseCase.getMetricsByQueue(eq("queue-001"), any(Instant.class), any(Instant.class)))
                .thenReturn(metrics);

        ResponseEntity<List<ConversationMetricResponse>> response = restTemplate.exchange(
                "/api/v1/metrics/queue/queue-001?from=" + FROM + "&to=" + TO,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).extracting(ConversationMetricResponse::conversationId)
                .containsExactlyInAnyOrder("c1", "c2");
    }

    @Test
    void getSummaryByQueue_returns200_withSummary() {
        ConversationMetricSummary summary =
                new ConversationMetricSummary(10L, 250.0, 2L, 20.0, "2024-01-01 to 2024-01-31");
        when(metricsUseCase.getSummaryByQueue(eq("queue-001"), any(Instant.class), any(Instant.class)))
                .thenReturn(summary);

        ResponseEntity<ConversationSummaryResponse> response = restTemplate.getForEntity(
                "/api/v1/metrics/queue/queue-001/summary?from=" + FROM + "&to=" + TO,
                ConversationSummaryResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().totalConversations()).isEqualTo(10L);
        assertThat(response.getBody().abandonRate()).isEqualTo(20.0);
        assertThat(response.getBody().period()).isEqualTo("2024-01-01 to 2024-01-31");
    }

    @Test
    void getMetricsByQueue_returns400_whenFromParamMissing() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/metrics/queue/queue-001?to=" + TO,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getMetricsByQueue_returns400_whenToParamMissing() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/v1/metrics/queue/queue-001?from=" + FROM,
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getAbandonedCalls_returns200_withAbandonedMetrics() {
        List<ConversationMetric> abandoned = List.of(
                TestFixtures.createConversationMetric("c1", true, 30L)
        );
        when(metricsUseCase.getAbandonedCalls(any(Instant.class), any(Instant.class)))
                .thenReturn(abandoned);

        ResponseEntity<List<ConversationMetricResponse>> response = restTemplate.exchange(
                "/api/v1/metrics/abandoned?from=" + FROM + "&to=" + TO,
                HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).abandoned()).isTrue();
    }
}

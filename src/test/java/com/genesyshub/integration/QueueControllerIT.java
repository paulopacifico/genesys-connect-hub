package com.genesyshub.integration;

import com.genesyshub.domain.model.DomainException;
import com.genesyshub.domain.model.Queue;
import com.genesyshub.domain.port.in.QueueUseCase;
import com.genesyshub.infrastructure.web.dto.ErrorResponse;
import com.genesyshub.infrastructure.web.dto.QueueResponse;
import com.genesyshub.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class QueueControllerIT extends AbstractIntegrationTest {

    @MockBean
    private QueueUseCase queueUseCase;

    @Test
    void listAllQueues_returns200_withQueueList() {
        List<Queue> queues = List.of(
                TestFixtures.createQueue("q1", "Support", List.of("voice")),
                TestFixtures.createQueue("q2", "Chat",    List.of("chat"))
        );
        when(queueUseCase.listAllQueues()).thenReturn(queues);

        ResponseEntity<List<QueueResponse>> response = restTemplate.exchange(
                "/api/v1/queues",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).extracting(QueueResponse::id)
                .containsExactlyInAnyOrder("q1", "q2");
    }

    @Test
    void findQueueById_returns200_whenFound() {
        Queue queue = TestFixtures.createQueue();
        when(queueUseCase.findQueueById("queue-001")).thenReturn(queue);

        ResponseEntity<QueueResponse> response = restTemplate.getForEntity(
                "/api/v1/queues/queue-001", QueueResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().id()).isEqualTo("queue-001");
        assertThat(response.getBody().name()).isEqualTo("Support Queue");
    }

    @Test
    void findQueueById_returns404_withErrorResponse_whenNotFound() {
        when(queueUseCase.findQueueById(anyString()))
                .thenThrow(new DomainException(DomainException.ErrorCode.QUEUE_NOT_FOUND,
                        "Queue not found: missing-q"));

        ResponseEntity<ErrorResponse> response = restTemplate.getForEntity(
                "/api/v1/queues/missing-q", ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errorCode()).isEqualTo("QUEUE_NOT_FOUND");
        assertThat(response.getBody().message()).contains("missing-q");
        assertThat(response.getBody().path()).isEqualTo("/api/v1/queues/missing-q");
    }

    @Test
    void findQueuesByMediaType_returns200_withFilteredList() {
        List<Queue> queues = List.of(TestFixtures.createQueue("q1", "Voice Q", List.of("voice")));
        when(queueUseCase.findQueuesByMediaType("voice")).thenReturn(queues);

        ResponseEntity<List<QueueResponse>> response = restTemplate.exchange(
                "/api/v1/queues/media/voice",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).mediaTypes()).contains("voice");
    }

    @Test
    void listAllQueues_returns200_withEmptyList_whenNoQueues() {
        when(queueUseCase.listAllQueues()).thenReturn(List.of());

        ResponseEntity<List<QueueResponse>> response = restTemplate.exchange(
                "/api/v1/queues",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }
}

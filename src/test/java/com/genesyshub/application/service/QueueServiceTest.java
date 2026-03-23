package com.genesyshub.application.service;

import com.genesyshub.domain.model.DomainException;
import com.genesyshub.domain.model.Queue;
import com.genesyshub.domain.port.out.QueuePort;
import com.genesyshub.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueServiceTest {

    @Mock
    private QueuePort queuePort;

    @InjectMocks
    private QueueService queueService;

    @Test
    void listAllQueues_returnsAllQueues_whenPortReturnsData() {
        List<Queue> queues = List.of(TestFixtures.createQueue(), TestFixtures.createQueue("q2", "Chat Queue", List.of("chat")));
        when(queuePort.fetchAllQueues()).thenReturn(queues);

        List<Queue> result = queueService.listAllQueues();

        assertThat(result).hasSize(2).containsExactlyElementsOf(queues);
    }

    @Test
    void listAllQueues_returnsEmptyList_whenPortReturnsNone() {
        when(queuePort.fetchAllQueues()).thenReturn(List.of());

        assertThat(queueService.listAllQueues()).isEmpty();
    }

    @Test
    void findQueueById_returnsQueue_whenExists() {
        Queue queue = TestFixtures.createQueue();
        when(queuePort.fetchQueueById("queue-001")).thenReturn(Optional.of(queue));

        Queue result = queueService.findQueueById("queue-001");

        assertThat(result).isEqualTo(queue);
        assertThat(result.id()).isEqualTo("queue-001");
        assertThat(result.name()).isEqualTo("Support Queue");
    }

    @Test
    void findQueueById_throwsDomainException_whenNotFound() {
        when(queuePort.fetchQueueById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> queueService.findQueueById("missing"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    assertThat(de.getCode()).isEqualTo(DomainException.ErrorCode.QUEUE_NOT_FOUND);
                    assertThat(de.getMessage()).contains("missing");
                });
    }

    @Test
    void findQueuesByMediaType_returnsFilteredQueues() {
        List<Queue> queues = List.of(
                TestFixtures.createQueue("q1", "Voice Queue", List.of("voice")),
                TestFixtures.createQueue("q2", "Chat Queue", List.of("chat")),
                TestFixtures.createQueue("q3", "Multi Queue", List.of("voice", "chat"))
        );
        when(queuePort.fetchAllQueues()).thenReturn(queues);

        List<Queue> result = queueService.findQueuesByMediaType("voice");

        assertThat(result).hasSize(2)
                .extracting(Queue::id)
                .containsExactlyInAnyOrder("q1", "q3");
    }

    @Test
    void findQueuesByMediaType_returnsEmpty_whenNoMatch() {
        List<Queue> queues = List.of(
                TestFixtures.createQueue("q1", "Chat Queue", List.of("chat"))
        );
        when(queuePort.fetchAllQueues()).thenReturn(queues);

        List<Queue> result = queueService.findQueuesByMediaType("email");

        assertThat(result).isEmpty();
    }

    @Test
    void findQueuesByMediaType_handlesNullMediaTypes() {
        Queue queueWithNullMedia = TestFixtures.createQueue("q1", "Queue", null);
        when(queuePort.fetchAllQueues()).thenReturn(List.of(queueWithNullMedia));

        assertThat(queueService.findQueuesByMediaType("voice")).isEmpty();
    }
}

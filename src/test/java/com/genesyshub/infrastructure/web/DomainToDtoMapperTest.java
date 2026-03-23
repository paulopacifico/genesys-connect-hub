package com.genesyshub.infrastructure.web;

import com.genesyshub.domain.model.Agent;
import com.genesyshub.domain.model.AgentStatus;
import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.model.Queue;
import com.genesyshub.infrastructure.web.dto.AgentResponse;
import com.genesyshub.infrastructure.web.dto.ConversationMetricResponse;
import com.genesyshub.infrastructure.web.dto.QueueResponse;
import com.genesyshub.infrastructure.web.mapper.DomainToDtoMapper;
import com.genesyshub.infrastructure.web.mapper.DomainToDtoMapperImpl;
import com.genesyshub.util.TestFixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainToDtoMapperTest {

    private final DomainToDtoMapper mapper = new DomainToDtoMapperImpl();

    @Test
    void queueToResponse_mapsAllFieldsCorrectly() {
        Queue queue = TestFixtures.createQueue();

        QueueResponse response = mapper.toQueueResponse(queue);

        assertThat(response.id()).isEqualTo("queue-001");
        assertThat(response.name()).isEqualTo("Support Queue");
        assertThat(response.division()).isEqualTo("Division A");
        assertThat(response.mediaTypes()).containsExactly("voice", "chat");
        assertThat(response.activeAgents()).isEqualTo(5);
        assertThat(response.onQueueAgents()).isEqualTo(3);
    }

    @Test
    void queueToResponse_handlesNullMediaTypes() {
        Queue queue = TestFixtures.createQueue("q1", "Queue", null);

        QueueResponse response = mapper.toQueueResponse(queue);

        assertThat(response.mediaTypes()).isNull();
    }

    @Test
    void agentToResponse_mapsStatusCorrectly() {
        Agent agent = TestFixtures.createAgent("a1", AgentStatus.ON_QUEUE);

        AgentResponse response = mapper.toAgentResponse(agent);

        assertThat(response.id()).isEqualTo("a1");
        assertThat(response.status()).isEqualTo("ON_QUEUE");
    }

    @Test
    void agentToResponse_mapsAllStatuses() {
        for (AgentStatus status : AgentStatus.values()) {
            Agent agent = TestFixtures.createAgent("a1", status);
            AgentResponse response = mapper.toAgentResponse(agent);
            assertThat(response.status()).isEqualTo(status.name());
        }
    }

    @Test
    void agentToResponse_mapsAllFieldsCorrectly() {
        Agent agent = TestFixtures.createAgent();

        AgentResponse response = mapper.toAgentResponse(agent);

        assertThat(response.id()).isEqualTo("agent-001");
        assertThat(response.name()).isEqualTo("John Doe");
        assertThat(response.email()).isEqualTo("john.doe@example.com");
        assertThat(response.division()).isEqualTo("Division A");
        assertThat(response.queueIds()).containsExactly("queue-001");
    }

    @Test
    void conversationMetricToResponse_mapsAllFields() {
        ConversationMetric metric = TestFixtures.createConversationMetric();

        ConversationMetricResponse response = mapper.toConversationMetricResponse(metric);

        assertThat(response.conversationId()).isEqualTo("conv-001");
        assertThat(response.queueId()).isEqualTo("queue-001");
        assertThat(response.agentId()).isEqualTo("agent-001");
        assertThat(response.mediaType()).isEqualTo("voice");
        assertThat(response.direction()).isEqualTo("inbound");
        assertThat(response.handleTimeSeconds()).isEqualTo(300L);
        assertThat(response.abandoned()).isFalse();
        assertThat(response.startTime()).isEqualTo(Instant.parse("2024-01-01T09:00:00Z"));
        assertThat(response.endTime()).isEqualTo(Instant.parse("2024-01-01T09:05:00Z"));
    }

    @Test
    void conversationMetricToResponse_mapsAbandonedCorrectly() {
        ConversationMetric metric = TestFixtures.createConversationMetric("c1", true, 30L);

        ConversationMetricResponse response = mapper.toConversationMetricResponse(metric);

        assertThat(response.abandoned()).isTrue();
        assertThat(response.handleTimeSeconds()).isEqualTo(30L);
    }

    @Test
    void toQueueResponseList_mapsAllItems() {
        List<Queue> queues = List.of(TestFixtures.createQueue(), TestFixtures.createQueue("q2", "Q2", List.of()));

        List<QueueResponse> responses = mapper.toQueueResponseList(queues);

        assertThat(responses).hasSize(2);
    }

    @Test
    void toQueueResponseList_returnsEmpty_forEmptyInput() {
        assertThat(mapper.toQueueResponseList(Collections.emptyList())).isEmpty();
    }

    @Test
    void toConversationMetricResponseList_mapsAllItems() {
        List<ConversationMetric> metrics = List.of(
                TestFixtures.createConversationMetric("c1", false, 200L),
                TestFixtures.createConversationMetric("c2", true, 50L)
        );

        List<ConversationMetricResponse> responses = mapper.toConversationMetricResponseList(metrics);

        assertThat(responses).hasSize(2);
        assertThat(responses).extracting(ConversationMetricResponse::abandoned)
                .containsExactly(false, true);
    }
}

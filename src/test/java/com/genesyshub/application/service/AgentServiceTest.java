package com.genesyshub.application.service;

import com.genesyshub.domain.model.Agent;
import com.genesyshub.domain.model.AgentStatus;
import com.genesyshub.domain.model.DomainException;
import com.genesyshub.domain.port.out.AgentPort;
import com.genesyshub.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

    @Mock
    private AgentPort agentPort;

    @InjectMocks
    private AgentService agentService;

    @Test
    void listAgentsByQueue_returnsAgents() {
        List<Agent> agents = List.of(
                TestFixtures.createAgent("a1", AgentStatus.ON_QUEUE),
                TestFixtures.createAgent("a2", AgentStatus.IDLE)
        );
        when(agentPort.fetchAgentsByQueue("queue-001")).thenReturn(agents);

        List<Agent> result = agentService.listAgentsByQueue("queue-001");

        assertThat(result).hasSize(2).containsExactlyElementsOf(agents);
    }

    @Test
    void listAgentsByQueue_returnsEmpty_whenNoAgents() {
        when(agentPort.fetchAgentsByQueue("queue-001")).thenReturn(List.of());

        assertThat(agentService.listAgentsByQueue("queue-001")).isEmpty();
    }

    @Test
    void findAgentById_returnsAgent_whenExists() {
        Agent agent = TestFixtures.createAgent();
        when(agentPort.fetchAgentById("agent-001")).thenReturn(Optional.of(agent));

        Agent result = agentService.findAgentById("agent-001");

        assertThat(result).isEqualTo(agent);
        assertThat(result.email()).isEqualTo("john.doe@example.com");
    }

    @Test
    void findAgentById_throwsException_whenNotFound() {
        when(agentPort.fetchAgentById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agentService.findAgentById("missing"))
                .isInstanceOf(DomainException.class)
                .satisfies(ex -> {
                    DomainException de = (DomainException) ex;
                    assertThat(de.getCode()).isEqualTo(DomainException.ErrorCode.AGENT_NOT_FOUND);
                    assertThat(de.getMessage()).contains("missing");
                });
    }

    @Test
    void getAgentStatusSummary_returnsCorrectCounts_groupedByStatus() {
        List<Agent> agents = List.of(
                TestFixtures.createAgent("a1", AgentStatus.ON_QUEUE),
                TestFixtures.createAgent("a2", AgentStatus.ON_QUEUE),
                TestFixtures.createAgent("a3", AgentStatus.IDLE),
                TestFixtures.createAgent("a4", AgentStatus.OFFLINE)
        );
        when(agentPort.fetchAgentsByQueue("queue-001")).thenReturn(agents);

        Map<AgentStatus, Long> summary = agentService.getAgentStatusSummary("queue-001");

        assertThat(summary)
                .containsEntry(AgentStatus.ON_QUEUE, 2L)
                .containsEntry(AgentStatus.IDLE, 1L)
                .containsEntry(AgentStatus.OFFLINE, 1L)
                .doesNotContainKey(AgentStatus.BUSY);
    }

    @Test
    void getAgentStatusSummary_handlesEmptyAgentList() {
        when(agentPort.fetchAgentsByQueue("queue-001")).thenReturn(List.of());

        Map<AgentStatus, Long> summary = agentService.getAgentStatusSummary("queue-001");

        assertThat(summary).isEmpty();
    }
}

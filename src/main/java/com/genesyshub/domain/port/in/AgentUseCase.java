package com.genesyshub.domain.port.in;

import com.genesyshub.domain.model.Agent;
import com.genesyshub.domain.model.AgentStatus;

import java.util.List;
import java.util.Map;

public interface AgentUseCase {

    List<Agent> listAgentsByQueue(String queueId);

    Agent findAgentById(String agentId);

    Map<AgentStatus, Long> getAgentStatusSummary(String queueId);
}

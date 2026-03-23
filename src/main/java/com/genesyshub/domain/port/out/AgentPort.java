package com.genesyshub.domain.port.out;

import com.genesyshub.domain.model.Agent;

import java.util.List;
import java.util.Optional;

public interface AgentPort {

    List<Agent> fetchAgentsByQueue(String queueId);

    Optional<Agent> fetchAgentById(String agentId);
}

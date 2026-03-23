package com.genesyshub.application.service;

import com.genesyshub.domain.model.Agent;
import com.genesyshub.domain.model.AgentStatus;
import com.genesyshub.domain.model.DomainException;
import com.genesyshub.domain.port.in.AgentUseCase;
import com.genesyshub.domain.port.out.AgentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService implements AgentUseCase {

    private final AgentPort agentPort;

    @Override
    public List<Agent> listAgentsByQueue(String queueId) {
        List<Agent> agents = agentPort.fetchAgentsByQueue(queueId);
        log.info("Listed {} agents for queueId={}", agents.size(), queueId);
        return agents;
    }

    @Override
    public Agent findAgentById(String agentId) {
        return agentPort.fetchAgentById(agentId)
                .orElseThrow(() -> new DomainException(
                        DomainException.ErrorCode.AGENT_NOT_FOUND,
                        "Agent not found: " + agentId));
    }

    @Override
    public Map<AgentStatus, Long> getAgentStatusSummary(String queueId) {
        return listAgentsByQueue(queueId).stream()
                .collect(Collectors.groupingBy(Agent::status, Collectors.counting()));
    }
}

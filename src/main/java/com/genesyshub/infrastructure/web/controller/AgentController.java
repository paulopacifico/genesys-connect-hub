package com.genesyshub.infrastructure.web.controller;

import com.genesyshub.domain.port.in.AgentUseCase;
import com.genesyshub.infrastructure.web.dto.AgentResponse;
import com.genesyshub.infrastructure.web.mapper.DomainToDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
@Tag(name = "Agents", description = "Genesys Cloud agent operations")
public class AgentController {

    private final AgentUseCase agentUseCase;
    private final DomainToDtoMapper mapper;

    @GetMapping("/queue/{queueId}")
    @Operation(summary = "List agents by queue")
    @ApiResponse(responseCode = "200", description = "Agents retrieved successfully")
    public List<AgentResponse> listAgentsByQueue(@PathVariable String queueId) {
        return mapper.toAgentResponseList(agentUseCase.listAgentsByQueue(queueId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find agent by ID")
    @ApiResponse(responseCode = "200", description = "Agent found")
    @ApiResponse(responseCode = "404", description = "Agent not found")
    public AgentResponse findAgentById(@PathVariable String id) {
        return mapper.toAgentResponse(agentUseCase.findAgentById(id));
    }

    @GetMapping("/queue/{queueId}/summary")
    @Operation(summary = "Get agent status summary for a queue")
    @ApiResponse(responseCode = "200", description = "Status summary retrieved successfully")
    public Map<String, Long> getAgentStatusSummary(@PathVariable String queueId) {
        return agentUseCase.getAgentStatusSummary(queueId).entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().name(), Map.Entry::getValue));
    }
}

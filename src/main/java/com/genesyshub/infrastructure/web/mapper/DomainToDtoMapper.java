package com.genesyshub.infrastructure.web.mapper;

import com.genesyshub.domain.model.Agent;
import com.genesyshub.domain.model.ApiHealthStatus;
import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.model.ConversationMetricSummary;
import com.genesyshub.domain.model.Queue;
import com.genesyshub.infrastructure.web.dto.AgentResponse;
import com.genesyshub.infrastructure.web.dto.ApiHealthResponse;
import com.genesyshub.infrastructure.web.dto.ConversationMetricResponse;
import com.genesyshub.infrastructure.web.dto.ConversationSummaryResponse;
import com.genesyshub.infrastructure.web.dto.QueueResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DomainToDtoMapper {

    QueueResponse toQueueResponse(Queue queue);

    List<QueueResponse> toQueueResponseList(List<Queue> queues);

    @Mapping(target = "status", expression = "java(agent.status().name())")
    AgentResponse toAgentResponse(Agent agent);

    List<AgentResponse> toAgentResponseList(List<Agent> agents);

    ConversationMetricResponse toConversationMetricResponse(ConversationMetric metric);

    List<ConversationMetricResponse> toConversationMetricResponseList(List<ConversationMetric> metrics);

    ConversationSummaryResponse toConversationSummaryResponse(ConversationMetricSummary summary);

    ApiHealthResponse toApiHealthResponse(ApiHealthStatus status);
}

package com.genesyshub.infrastructure.genesys;

import com.genesyshub.domain.model.Agent;
import com.genesyshub.domain.model.AgentStatus;
import com.genesyshub.domain.model.DomainException;
import com.mypurecloud.sdk.v2.ApiException;
import com.mypurecloud.sdk.v2.api.RoutingApi;
import com.mypurecloud.sdk.v2.api.UsersApi;
import com.mypurecloud.sdk.v2.model.QueueMember;
import com.mypurecloud.sdk.v2.model.QueueMemberEntityListing;
import com.mypurecloud.sdk.v2.model.RoutingStatus;
import com.mypurecloud.sdk.v2.model.User;
import com.genesyshub.domain.port.out.AgentPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenesysAgentAdapter implements AgentPort {

    private static final int PAGE_SIZE = 100;
    private static final List<String> USER_EXPAND = List.of("routingStatus", "presence", "conversationSummary");

    private final RoutingApi routingApi;
    private final UsersApi usersApi;

    @Override
    public List<Agent> fetchAgentsByQueue(String queueId) {
        log.debug("Fetching agents for queue id={}", queueId);
        long start = System.currentTimeMillis();

        List<Agent> result = new ArrayList<>();
        int pageNumber = 1;

        try {
            QueueMemberEntityListing page;
            do {
                page = routingApi.getRoutingQueueMembers(queueId, pageNumber, PAGE_SIZE,
                        null, null, null, null, null);

                if (page.getEntities() != null) {
                    for (QueueMember member : page.getEntities()) {
                        result.add(memberToDomain(member));
                    }
                }
                pageNumber++;
            } while (page.getNextUri() != null);

            log.info("Fetched {} agents for queue id={} in {}ms",
                    result.size(), queueId, System.currentTimeMillis() - start);
            return result;

        } catch (ApiException e) {
            log.error("Failed to fetch agents for queue id={}: status={}, message={}",
                    queueId, e.getStatusCode(), e.getMessage());
            throw new DomainException(DomainException.ErrorCode.GENESYS_API_ERROR,
                    "Failed to fetch agents for queue " + queueId + ": " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Agent> fetchAgentById(String agentId) {
        log.debug("Fetching agent id={}", agentId);
        long start = System.currentTimeMillis();

        try {
            User user = usersApi.getUser(agentId, USER_EXPAND, null, null);
            log.info("Fetched agent id={} in {}ms", agentId, System.currentTimeMillis() - start);
            return Optional.of(userToDomain(user));

        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                log.warn("Agent not found: id={}", agentId);
                return Optional.empty();
            }
            log.error("Failed to fetch agent id={}: status={}, message={}",
                    agentId, e.getStatusCode(), e.getMessage());
            throw new DomainException(DomainException.ErrorCode.GENESYS_API_ERROR,
                    "Failed to fetch agent " + agentId + ": " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------

    private Agent memberToDomain(QueueMember member) {
        String division = member.getDivision() != null ? member.getDivision().getName() : null;
        AgentStatus status = mapRoutingStatus(member.getRoutingStatus());

        return new Agent(
                member.getId(),
                member.getName(),
                null,
                status,
                division,
                Collections.emptyList(),
                Instant.now()
        );
    }

    private Agent userToDomain(User user) {
        String division = user.getDivision() != null ? user.getDivision().getName() : null;
        AgentStatus status = mapRoutingStatus(user.getRoutingStatus());

        Instant lastUpdated = user.getRoutingStatus() != null && user.getRoutingStatus().getStartTime() != null
                ? user.getRoutingStatus().getStartTime().toInstant()
                : Instant.now();

        return new Agent(
                user.getId(),
                user.getName(),
                user.getEmail(),
                status,
                division,
                Collections.emptyList(),
                lastUpdated
        );
    }

    private AgentStatus mapRoutingStatus(RoutingStatus routingStatus) {
        if (routingStatus == null || routingStatus.getStatus() == null) {
            return AgentStatus.OFFLINE;
        }
        return switch (routingStatus.getStatus()) {
            case ON_QUEUE -> AgentStatus.ON_QUEUE;
            case OFF_QUEUE -> AgentStatus.OFF_QUEUE;
            case IDLE -> AgentStatus.IDLE;
            case INTERACTING -> AgentStatus.BUSY;
            case NOT_RESPONDING -> AgentStatus.AWAY;
            default -> AgentStatus.OFFLINE;
        };
    }
}

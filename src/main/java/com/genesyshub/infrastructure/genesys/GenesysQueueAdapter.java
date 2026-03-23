package com.genesyshub.infrastructure.genesys;

import com.genesyshub.domain.model.DomainException;
import com.genesyshub.domain.model.Queue;
import com.genesyshub.domain.port.out.QueuePort;
import com.mypurecloud.sdk.v2.ApiException;
import com.mypurecloud.sdk.v2.api.RoutingApi;
import com.mypurecloud.sdk.v2.model.QueueEntityListing;
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
public class GenesysQueueAdapter implements QueuePort {

    private static final int PAGE_SIZE = 100;

    private final RoutingApi routingApi;

    @Override
    public List<Queue> fetchAllQueues() {
        log.debug("Fetching all queues from Genesys Cloud");
        long start = System.currentTimeMillis();

        List<Queue> result = new ArrayList<>();
        int pageNumber = 1;

        try {
            QueueEntityListing page;
            do {
                page = routingApi.getRoutingQueues(
                        PAGE_SIZE, pageNumber, null, null, null, null, null, null, null, null, null);

                if (page.getEntities() != null) {
                    page.getEntities().stream()
                            .map(this::toDomain)
                            .forEach(result::add);
                }
                pageNumber++;
            // getNextUri() returns null (not empty string) when there are no more pages
            } while (page.getNextUri() != null);

            log.info("Fetched {} queues from Genesys Cloud in {}ms",
                    result.size(), System.currentTimeMillis() - start);
            return result;

        } catch (ApiException e) {
            log.error("Failed to fetch queues: status={}, message={}", e.getStatusCode(), e.getMessage());
            throw new DomainException(DomainException.ErrorCode.GENESYS_API_ERROR,
                    "Failed to fetch queues: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Queue> fetchQueueById(String queueId) {
        log.debug("Fetching queue by id={}", queueId);
        long start = System.currentTimeMillis();

        try {
            com.mypurecloud.sdk.v2.model.Queue sdkQueue = routingApi.getRoutingQueue(queueId);
            log.info("Fetched queue id={} in {}ms", queueId, System.currentTimeMillis() - start);
            return Optional.of(toDomain(sdkQueue));

        } catch (ApiException e) {
            if (e.getStatusCode() == 404) {
                log.warn("Queue not found: id={}", queueId);
                return Optional.empty();
            }
            log.error("Failed to fetch queue id={}: status={}, message={}",
                    queueId, e.getStatusCode(), e.getMessage());
            throw new DomainException(DomainException.ErrorCode.GENESYS_API_ERROR,
                    "Failed to fetch queue " + queueId + ": " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------

    private Queue toDomain(com.mypurecloud.sdk.v2.model.Queue sdkQueue) {
        List<String> mediaTypes = sdkQueue.getMediaSettings() != null
                ? new ArrayList<>(sdkQueue.getMediaSettings().keySet())
                : Collections.emptyList();

        String division = sdkQueue.getDivision() != null ? sdkQueue.getDivision().getName() : null;

        Instant createdAt = sdkQueue.getDateCreated() != null
                ? sdkQueue.getDateCreated().toInstant()
                : null;

        int activeAgents = sdkQueue.getMemberCount() != null ? sdkQueue.getMemberCount() : 0;

        return new Queue(
                sdkQueue.getId(),
                sdkQueue.getName(),
                division,
                mediaTypes,
                activeAgents,
                0,
                0,
                createdAt
        );
    }
}

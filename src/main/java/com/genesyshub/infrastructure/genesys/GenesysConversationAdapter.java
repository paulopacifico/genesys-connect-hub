package com.genesyshub.infrastructure.genesys;

import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.domain.model.DomainException;
import com.genesyshub.domain.port.out.ConversationMetricPort;
import com.mypurecloud.sdk.v2.ApiException;
import com.mypurecloud.sdk.v2.api.AnalyticsApi;
import com.mypurecloud.sdk.v2.model.AnalyticsConversationQueryResponse;
import com.mypurecloud.sdk.v2.model.AnalyticsConversationWithoutAttributes;
import com.mypurecloud.sdk.v2.model.AnalyticsQueryPredicate;
import com.mypurecloud.sdk.v2.model.ConversationQuery;
import com.mypurecloud.sdk.v2.model.SegmentDetailQueryFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenesysConversationAdapter implements ConversationMetricPort {

    private final AnalyticsApi analyticsApi;

    @Override
    public List<ConversationMetric> fetchConversationMetrics(String queueId, Instant from, Instant to) {
        log.debug("Fetching conversation metrics: queueId={}, from={}, to={}", queueId, from, to);
        long start = System.currentTimeMillis();

        ConversationQuery query = buildQuery(queueId, from, to);

        try {
            AnalyticsConversationQueryResponse response =
                    analyticsApi.postAnalyticsConversationsDetailsQuery(query);

            List<ConversationMetric> metrics = mapToMetrics(response, queueId);
            log.info("Fetched {} conversation metrics for queueId={} in {}ms",
                    metrics.size(), queueId, System.currentTimeMillis() - start);
            return metrics;

        } catch (ApiException e) {
            log.error("Failed to fetch conversation metrics for queueId={}: status={}, message={}",
                    queueId, e.getStatusCode(), e.getMessage());
            throw new DomainException(DomainException.ErrorCode.GENESYS_API_ERROR,
                    "Failed to fetch conversation metrics: " + e.getMessage(), e);
        }
    }

    // -------------------------------------------------------------------------

    private ConversationQuery buildQuery(String queueId, Instant from, Instant to) {
        AnalyticsQueryPredicate queuePredicate = new AnalyticsQueryPredicate()
                .type(AnalyticsQueryPredicate.TypeEnum.DIMENSION)
                .dimension(AnalyticsQueryPredicate.DimensionEnum.QUEUEID)
                .operator(AnalyticsQueryPredicate.OperatorEnum.MATCHES)
                .value(queueId);

        SegmentDetailQueryFilter filter = new SegmentDetailQueryFilter()
                .type(SegmentDetailQueryFilter.TypeEnum.AND)
                .addPredicatesItem(queuePredicate);

        return new ConversationQuery()
                .interval(from.toString() + "/" + to.toString())
                .addSegmentFiltersItem(filter);
    }

    private List<ConversationMetric> mapToMetrics(
            AnalyticsConversationQueryResponse response, String queueId) {

        if (response == null || response.getConversations() == null) {
            return Collections.emptyList();
        }

        List<ConversationMetric> metrics = new ArrayList<>();
        for (AnalyticsConversationWithoutAttributes conv : response.getConversations()) {
            Instant startTime = conv.getConversationStart() != null
                    ? conv.getConversationStart().toInstant() : null;
            Instant endTime = conv.getConversationEnd() != null
                    ? conv.getConversationEnd().toInstant() : null;

            long handleTime = (startTime != null && endTime != null)
                    ? endTime.getEpochSecond() - startTime.getEpochSecond()
                    : 0L;

            metrics.add(new ConversationMetric(
                    conv.getConversationId(),
                    queueId,
                    null,
                    extractMediaType(conv),                                                              // mediaType
                    conv.getOriginatingDirection() != null ? conv.getOriginatingDirection().toString() : null,  // direction
                    startTime,
                    endTime,
                    handleTime,
                    isAbandoned(conv)
            ));
        }
        return metrics;
    }

    /**
     * Derives the media type from the first session found across all participants.
     * Defaults to "voice" when participant or session data is absent.
     */
    private String extractMediaType(AnalyticsConversationWithoutAttributes conv) {
        if (conv.getParticipants() == null) {
            log.debug("No participant data for conversationId={}; defaulting mediaType to 'voice'",
                    conv.getConversationId());
            return "voice";
        }
        return conv.getParticipants().stream()
                .filter(p -> p.getSessions() != null && !p.getSessions().isEmpty())
                .flatMap(p -> p.getSessions().stream())
                .map(s -> s.getMediaType() != null ? s.getMediaType().toString().toLowerCase() : null)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse("voice");
    }

    /**
     * A conversation is considered abandoned when a queue/IVR segment was reached
     * but no agent INTERACT segment exists — i.e. the customer hung up before an
     * agent answered.
     * Returns false when participant detail data is not present in the response.
     */
    private boolean isAbandoned(AnalyticsConversationWithoutAttributes conv) {
        if (conv.getParticipants() == null) {
            log.debug("No participant data for conversationId={}; defaulting abandoned to false",
                    conv.getConversationId());
            return false;
        }
        boolean hasQueueSegment = conv.getParticipants().stream()
                .anyMatch(p -> "acd".equalsIgnoreCase(p.getPurpose()) || "ivr".equalsIgnoreCase(p.getPurpose()));
        boolean hasAgentInteraction = conv.getParticipants().stream()
                .filter(p -> "agent".equalsIgnoreCase(p.getPurpose()))
                .flatMap(p -> p.getSessions() != null ? p.getSessions().stream() : java.util.stream.Stream.empty())
                .flatMap(s -> s.getSegments() != null ? s.getSegments().stream() : java.util.stream.Stream.empty())
                .anyMatch(seg -> "interact".equalsIgnoreCase(
                        seg.getSegmentType() != null ? seg.getSegmentType().toString() : ""));
        return hasQueueSegment && !hasAgentInteraction;
    }
}

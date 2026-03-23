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
import java.util.Date;
import java.util.List;

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
                    conv.getOriginatingDirection() != null ? conv.getOriginatingDirection().toString() : null,
                    conv.getOriginatingDirection() != null ? conv.getOriginatingDirection().toString() : null,
                    startTime,
                    endTime,
                    handleTime,
                    false
            ));
        }
        return metrics;
    }
}

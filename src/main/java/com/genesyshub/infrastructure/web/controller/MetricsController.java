package com.genesyshub.infrastructure.web.controller;

import com.genesyshub.domain.port.in.MetricsUseCase;
import com.genesyshub.infrastructure.web.dto.ConversationMetricResponse;
import com.genesyshub.infrastructure.web.dto.ConversationSummaryResponse;
import com.genesyshub.infrastructure.web.mapper.DomainToDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Conversation metrics and analytics")
public class MetricsController {

    private final MetricsUseCase metricsUseCase;
    private final DomainToDtoMapper mapper;

    @GetMapping("/queue/{queueId}")
    @Operation(summary = "Get conversation metrics for a queue")
    @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid date parameters")
    public List<ConversationMetricResponse> getMetricsByQueue(
            @PathVariable String queueId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return mapper.toConversationMetricResponseList(metricsUseCase.getMetricsByQueue(queueId, from, to));
    }

    @GetMapping("/queue/{queueId}/summary")
    @Operation(summary = "Get conversation metrics summary for a queue")
    @ApiResponse(responseCode = "200", description = "Summary retrieved successfully")
    public ConversationSummaryResponse getSummaryByQueue(
            @PathVariable String queueId,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return mapper.toConversationSummaryResponse(metricsUseCase.getSummaryByQueue(queueId, from, to));
    }

    @GetMapping("/abandoned")
    @Operation(summary = "Get abandoned calls within a time range")
    @ApiResponse(responseCode = "200", description = "Abandoned calls retrieved successfully")
    public List<ConversationMetricResponse> getAbandonedCalls(
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return mapper.toConversationMetricResponseList(metricsUseCase.getAbandonedCalls(from, to));
    }
}

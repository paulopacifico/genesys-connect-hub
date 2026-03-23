package com.genesyshub.infrastructure.web.controller;

import com.genesyshub.domain.port.in.QueueUseCase;
import com.genesyshub.infrastructure.web.dto.QueueResponse;
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

@RestController
@RequestMapping("/api/v1/queues")
@RequiredArgsConstructor
@Tag(name = "Queues", description = "Genesys Cloud queue operations")
public class QueueController {

    private final QueueUseCase queueUseCase;
    private final DomainToDtoMapper mapper;

    @GetMapping
    @Operation(summary = "List all queues")
    @ApiResponse(responseCode = "200", description = "Queues retrieved successfully")
    public List<QueueResponse> listAllQueues() {
        return mapper.toQueueResponseList(queueUseCase.listAllQueues());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find queue by ID")
    @ApiResponse(responseCode = "200", description = "Queue found")
    @ApiResponse(responseCode = "404", description = "Queue not found")
    public QueueResponse findQueueById(@PathVariable String id) {
        return mapper.toQueueResponse(queueUseCase.findQueueById(id));
    }

    @GetMapping("/media/{type}")
    @Operation(summary = "Find queues by media type")
    @ApiResponse(responseCode = "200", description = "Queues retrieved successfully")
    public List<QueueResponse> findQueuesByMediaType(@PathVariable String type) {
        return mapper.toQueueResponseList(queueUseCase.findQueuesByMediaType(type));
    }
}

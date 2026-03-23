package com.genesyshub.infrastructure.web.controller;

import com.genesyshub.domain.port.in.HealthCheckUseCase;
import com.genesyshub.infrastructure.web.dto.ApiHealthResponse;
import com.genesyshub.infrastructure.web.mapper.DomainToDtoMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
@RequiredArgsConstructor
@Tag(name = "Health", description = "API connectivity health checks")
public class HealthController {

    private final HealthCheckUseCase healthCheckUseCase;
    private final DomainToDtoMapper mapper;

    @GetMapping("/genesys")
    @Operation(summary = "Check Genesys Cloud API connectivity")
    @ApiResponse(responseCode = "200", description = "Health status returned")
    public ApiHealthResponse checkGenesysConnection() {
        return mapper.toApiHealthResponse(healthCheckUseCase.checkConnection());
    }
}

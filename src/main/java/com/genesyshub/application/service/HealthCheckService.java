package com.genesyshub.application.service;

import com.genesyshub.domain.model.ApiHealthStatus;
import com.genesyshub.domain.port.in.HealthCheckUseCase;
import com.genesyshub.domain.port.out.GenesysHealthPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthCheckService implements HealthCheckUseCase {

    private final GenesysHealthPort genesysHealthPort;

    @Override
    public ApiHealthStatus checkConnection() {
        ApiHealthStatus status = genesysHealthPort.checkApiHealth();
        log.info("Genesys health check: connected={}, org={}, region={}",
                status.connected(), status.organizationName(), status.region());
        return status;
    }
}

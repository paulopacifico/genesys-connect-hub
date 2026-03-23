package com.genesyshub.domain.port.in;

import com.genesyshub.domain.model.ApiHealthStatus;

public interface HealthCheckUseCase {

    ApiHealthStatus checkConnection();
}

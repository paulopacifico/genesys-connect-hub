package com.genesyshub.domain.port.out;

import com.genesyshub.domain.model.ApiHealthStatus;

public interface GenesysHealthPort {

    ApiHealthStatus checkApiHealth();
}

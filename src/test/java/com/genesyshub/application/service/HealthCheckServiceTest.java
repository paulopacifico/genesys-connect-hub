package com.genesyshub.application.service;

import com.genesyshub.domain.model.ApiHealthStatus;
import com.genesyshub.domain.port.out.GenesysHealthPort;
import com.genesyshub.util.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private GenesysHealthPort genesysHealthPort;

    @InjectMocks
    private HealthCheckService healthCheckService;

    @Test
    void checkConnection_returnsConnectedStatus() {
        ApiHealthStatus status = TestFixtures.createHealthStatus(true);
        when(genesysHealthPort.checkApiHealth()).thenReturn(status);

        ApiHealthStatus result = healthCheckService.checkConnection();

        assertThat(result.connected()).isTrue();
        assertThat(result.organizationName()).isEqualTo("Acme Corp");
        assertThat(result.region()).isEqualTo("mypurecloud.com");
    }

    @Test
    void checkConnection_returnsDisconnectedStatus_whenHealthFails() {
        ApiHealthStatus status = TestFixtures.createHealthStatus(false);
        when(genesysHealthPort.checkApiHealth()).thenReturn(status);

        ApiHealthStatus result = healthCheckService.checkConnection();

        assertThat(result.connected()).isFalse();
    }
}

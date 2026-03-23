package com.genesyshub.infrastructure.genesys;

import com.genesyshub.domain.model.ApiHealthStatus;
import com.genesyshub.domain.model.DomainException;
import com.genesyshub.domain.port.out.GenesysHealthPort;
import com.mypurecloud.sdk.v2.ApiException;
import com.mypurecloud.sdk.v2.api.OrganizationsApi;
import com.mypurecloud.sdk.v2.model.Organization;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class GenesysHealthAdapter implements GenesysHealthPort {

    private final OrganizationsApi organizationsApi;
    private final String region;

    public GenesysHealthAdapter(OrganizationsApi organizationsApi,
                                @Value("${genesys.region}") String region) {
        this.organizationsApi = organizationsApi;
        this.region = region;
    }

    @Override
    public ApiHealthStatus checkApiHealth() {
        log.debug("Checking Genesys Cloud API health");
        long start = System.currentTimeMillis();

        try {
            Organization org = organizationsApi.getOrganizationsMe(null);
            log.info("Genesys Cloud health check OK: org={}, region={}, duration={}ms",
                    org.getName(), region, System.currentTimeMillis() - start);

            return new ApiHealthStatus(true, region, org.getName(), Instant.now());

        } catch (ApiException e) {
            log.error("Genesys Cloud health check failed: status={}, message={}",
                    e.getStatusCode(), e.getMessage());
            return new ApiHealthStatus(false, region, null, Instant.now());
        }
    }
}

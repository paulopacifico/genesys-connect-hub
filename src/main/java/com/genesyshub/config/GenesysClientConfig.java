package com.genesyshub.config;

import com.genesyshub.domain.model.DomainException;
import com.mypurecloud.sdk.v2.ApiClient;
import com.mypurecloud.sdk.v2.ApiException;
import com.mypurecloud.sdk.v2.PureCloudRegionHosts;
import com.mypurecloud.sdk.v2.api.AnalyticsApi;
import com.mypurecloud.sdk.v2.api.ConversationsApi;
import com.mypurecloud.sdk.v2.api.OrganizationsApi;
import com.mypurecloud.sdk.v2.api.RoutingApi;
import com.mypurecloud.sdk.v2.api.UsersApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;

@org.springframework.context.annotation.Configuration
public class GenesysClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(GenesysClientConfig.class);

    private String clientId;
    private String clientSecret;
    private ApiClient apiClient;

    @Bean
    public ApiClient genesysApiClient(GenesysProperties properties) {
        this.clientId = properties.clientId();
        this.clientSecret = properties.clientSecret();

        String maskedClientId = maskClientId(clientId);
        logger.info("Initializing Genesys Cloud client for region={}, clientId={}...",
                properties.region(), maskedClientId);

        PureCloudRegionHosts region = resolveRegion(properties.region());
        ApiClient client = ApiClient.Builder.standard()
                .withBasePath(region)
                .build();

        try {
            var authResponse = client.authorizeClientCredentials(clientId, clientSecret);
            logger.info("Genesys Cloud authenticated successfully (clientId={}, expires_in={}s)",
                    maskedClientId, authResponse.getExpiresIn());
        } catch (ApiException e) {
            logger.error("Genesys Cloud authentication failed for clientId={}: {}",
                    maskedClientId, e.getMessage());
            throw new DomainException(DomainException.ErrorCode.GENESYS_AUTH_FAILED,
                    "Failed to authenticate with Genesys Cloud: " + e.getMessage(), e);
        }

        this.apiClient = client;
        return client;
    }

    @Scheduled(fixedDelayString = "${genesys.token-refresh-interval-ms:3600000}")
    public void refreshToken() {
        try {
            logger.info("Refreshing Genesys Cloud token for clientId={}...", maskClientId(clientId));
            apiClient.authorizeClientCredentials(clientId, clientSecret);
            logger.info("Genesys Cloud token refreshed successfully");
        } catch (ApiException e) {
            logger.error("Genesys Cloud token refresh failed: {}", e.getMessage());
            throw new DomainException(DomainException.ErrorCode.GENESYS_AUTH_FAILED,
                    "Failed to refresh Genesys Cloud token: " + e.getMessage(), e);
        }
    }

    @Bean
    public RoutingApi routingApi(ApiClient apiClient) {
        return new RoutingApi(apiClient);
    }

    @Bean
    public UsersApi usersApi(ApiClient apiClient) {
        return new UsersApi(apiClient);
    }

    @Bean
    public ConversationsApi conversationsApi(ApiClient apiClient) {
        return new ConversationsApi(apiClient);
    }

    @Bean
    public AnalyticsApi analyticsApi(ApiClient apiClient) {
        return new AnalyticsApi(apiClient);
    }

    @Bean
    public OrganizationsApi organizationsApi(ApiClient apiClient) {
        return new OrganizationsApi(apiClient);
    }

    // -------------------------------------------------------------------------

    private String maskClientId(String clientId) {
        if (clientId == null || clientId.length() <= 4) return "****";
        return clientId.substring(0, 4) + "****";
    }

    private PureCloudRegionHosts resolveRegion(String region) {
        for (PureCloudRegionHosts host : PureCloudRegionHosts.values()) {
            if (host.getApiHost().contains(region)) {
                return host;
            }
        }
        logger.warn("Unknown Genesys region '{}', falling back to us_east_1", region);
        return PureCloudRegionHosts.us_east_1;
    }
}

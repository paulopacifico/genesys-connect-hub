package com.genesyshub.config;

import com.genesyshub.domain.port.out.AgentPort;
import com.genesyshub.domain.port.out.ConversationMetricPort;
import com.genesyshub.domain.port.out.GenesysHealthPort;
import com.genesyshub.domain.port.out.QueuePort;
import com.mypurecloud.sdk.v2.ApiClient;
import com.mypurecloud.sdk.v2.api.AnalyticsApi;
import com.mypurecloud.sdk.v2.api.ConversationsApi;
import com.mypurecloud.sdk.v2.api.OrganizationsApi;
import com.mypurecloud.sdk.v2.api.RoutingApi;
import com.mypurecloud.sdk.v2.api.UsersApi;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Replaces all Genesys Cloud SDK beans with mocks for integration tests,
 * so the application context starts without real API credentials.
 */
@TestConfiguration
public class GenesysTestConfig {

    @Bean
    @Primary
    public ApiClient genesysApiClient() {
        return Mockito.mock(ApiClient.class);
    }

    @Bean
    @Primary
    public RoutingApi routingApi() {
        return Mockito.mock(RoutingApi.class);
    }

    @Bean
    @Primary
    public UsersApi usersApi() {
        return Mockito.mock(UsersApi.class);
    }

    @Bean
    @Primary
    public ConversationsApi conversationsApi() {
        return Mockito.mock(ConversationsApi.class);
    }

    @Bean
    @Primary
    public AnalyticsApi analyticsApi() {
        return Mockito.mock(AnalyticsApi.class);
    }

    @Bean
    @Primary
    public OrganizationsApi organizationsApi() {
        return Mockito.mock(OrganizationsApi.class);
    }

    @Bean
    @Primary
    public QueuePort queuePort() {
        return Mockito.mock(QueuePort.class);
    }

    @Bean
    @Primary
    public AgentPort agentPort() {
        return Mockito.mock(AgentPort.class);
    }

    @Bean
    @Primary
    public ConversationMetricPort conversationMetricPort() {
        return Mockito.mock(ConversationMetricPort.class);
    }

    @Bean
    @Primary
    public GenesysHealthPort genesysHealthPort() {
        return Mockito.mock(GenesysHealthPort.class);
    }
}

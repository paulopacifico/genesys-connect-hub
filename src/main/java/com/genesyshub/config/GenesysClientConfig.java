package com.genesyshub.config;

import com.mypurecloud.sdk.v2.ApiClient;
import com.mypurecloud.sdk.v2.ApiException;
import com.mypurecloud.sdk.v2.Configuration;
import com.mypurecloud.sdk.v2.auth.AuthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class GenesysClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(GenesysClientConfig.class);

    @Bean
    public ApiClient genesysApiClient(GenesysProperties properties) throws ApiException {
        ApiClient client = ApiClient.Builder.standard()
                .withBasePath("https://api." + properties.region())
                .build();

        AuthResponse authResponse = client.authorizeClientCredentials(
                properties.clientId(), properties.clientSecret());

        logger.info("Genesys Cloud authenticated, token expires in {} seconds",
                authResponse.getExpiresIn());

        Configuration.setDefaultApiClient(client);
        return client;
    }
}

package com.genesyshub.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "genesys")
public record GenesysProperties(
        @NotBlank String clientId,
        @NotBlank String clientSecret,
        @NotBlank String region
) {}

package com.genesyshub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GenesysConnectHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(GenesysConnectHubApplication.class, args);
    }
}

package com.genesyshub.infrastructure.persistence.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.genesyshub.domain.model.WebhookEvent;
import com.genesyshub.infrastructure.persistence.entity.WebhookEventEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Mapper(componentModel = "spring")
public abstract class WebhookEventMapper {

    @Autowired
    protected ObjectMapper objectMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "processed", constant = "false")
    @Mapping(target = "processedAt", ignore = true)
    @Mapping(target = "payload", source = "payload", qualifiedByName = "mapToJson")
    public abstract WebhookEventEntity toEntity(WebhookEvent domain);

    @Mapping(target = "payload", source = "payload", qualifiedByName = "mapFromJson")
    public abstract WebhookEvent toDomain(WebhookEventEntity entity);

    @Named("mapToJson")
    protected String mapToJson(Map<String, Object> payload) {
        if (payload == null) return null;
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize webhook payload", e);
        }
    }

    @Named("mapFromJson")
    @SuppressWarnings("unchecked")
    protected Map<String, Object> mapFromJson(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to deserialize webhook payload", e);
        }
    }
}

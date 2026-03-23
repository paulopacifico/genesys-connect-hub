package com.genesyshub.infrastructure.persistence.mapper;

import com.genesyshub.domain.model.ConversationMetric;
import com.genesyshub.infrastructure.persistence.entity.ConversationMetricEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMetricMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    ConversationMetricEntity toEntity(ConversationMetric domain);

    ConversationMetric toDomain(ConversationMetricEntity entity);

    List<ConversationMetric> toDomainList(List<ConversationMetricEntity> entities);
}

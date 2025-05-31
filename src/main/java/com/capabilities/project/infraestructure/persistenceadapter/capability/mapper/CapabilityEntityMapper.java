package com.capabilities.project.infraestructure.persistenceadapter.capability.mapper;

import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.infraestructure.persistenceadapter.capability.entity.CapabilityEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CapabilityEntityMapper {
    Capability toModel(CapabilityEntity capabilityEntity);
    CapabilityEntity toEntity(Capability capability);
}

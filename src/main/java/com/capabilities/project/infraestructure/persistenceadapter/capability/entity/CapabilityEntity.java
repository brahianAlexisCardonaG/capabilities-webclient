package com.capabilities.project.infraestructure.persistenceadapter.capability.entity;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "capability")
@Data
@RequiredArgsConstructor
public class CapabilityEntity {
    @Id
    private Long id;
    private String name;
    private String description;
}

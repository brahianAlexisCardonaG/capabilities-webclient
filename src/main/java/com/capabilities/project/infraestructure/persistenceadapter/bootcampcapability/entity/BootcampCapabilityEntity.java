package com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("capability_bootcamp")
@Data
@AllArgsConstructor
public class BootcampCapabilityEntity {
    @Id
    private Long id;

    @Column("id_bootcamp")
    private Long idBootcamp;

    @Column("id_capability")
    private Long idCapability;
}
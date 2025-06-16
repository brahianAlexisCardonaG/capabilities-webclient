package com.capabilities.project.infraestructure.entrypoints.util.response.capability;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CapabilityResponse {
    private Long id;
    private String name;
    private String description;
    private List<Long> technologyIds;
}

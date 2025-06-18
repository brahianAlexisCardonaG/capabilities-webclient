package com.capabilities.project.domain.model.capability;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Capability {
    private Long id;
    private String name;
    private String description;
    private List<Long> technologyIds;
}

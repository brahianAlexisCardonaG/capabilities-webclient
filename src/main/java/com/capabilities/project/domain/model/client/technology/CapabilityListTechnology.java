package com.capabilities.project.domain.model.client.technology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CapabilityListTechnology {
    private Long id;
    private String name;
    private List<Technology> technologies;
}

package com.capabilities.project.infraestructure.persistenceadapter.webclients.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CapabilityListTechnologyResponse {
    private Long id;
    private String name;
    private List<TechnologyResponse> technologies;
}

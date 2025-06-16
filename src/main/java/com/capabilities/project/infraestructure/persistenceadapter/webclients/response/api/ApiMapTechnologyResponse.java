package com.capabilities.project.infraestructure.persistenceadapter.webclients.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiCapabilityTechnologyResponse {
    private String code;
    private String message;
    private String date;
    private Map<String, List<TechnologyResponse>> data;
}

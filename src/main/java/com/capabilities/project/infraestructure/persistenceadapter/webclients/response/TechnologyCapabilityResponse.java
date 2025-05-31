package com.capabilities.project.infraestructure.persistenceadapter.webclients.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class TechnologyCapabilityResponse {
    private String code;
    private String message;
    private String identifier;
    private String date;
    private Map<String, List<TechnologyResponse>> data;
}

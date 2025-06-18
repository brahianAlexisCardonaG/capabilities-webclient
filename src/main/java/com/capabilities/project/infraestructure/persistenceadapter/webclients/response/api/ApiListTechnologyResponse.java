package com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api;

import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiListTechnologyResponse {
    private String code;
    private String message;
    private String date;
    private List<TechnologyResponse> data;
}

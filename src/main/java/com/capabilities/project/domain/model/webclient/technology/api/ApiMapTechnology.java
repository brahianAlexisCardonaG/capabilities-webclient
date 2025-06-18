package com.capabilities.project.domain.model.webclient.technology.api;

import com.capabilities.project.domain.model.webclient.technology.Technology;
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
public class ApiMapTechnology{
    private String code;
    private String message;
    private String date;
    private Map<String, List<Technology>> data;
}

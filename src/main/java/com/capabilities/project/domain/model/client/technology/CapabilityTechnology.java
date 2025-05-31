package com.capabilities.project.domain.model.client.technology;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CapabilityTechnology {
    private String code;
    private String message;
    private String identifier;
    private String date;
    private Map<String, List<Technology>> data;
}

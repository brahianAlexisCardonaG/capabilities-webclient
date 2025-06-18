package com.capabilities.project.domain.model.capability;

import com.capabilities.project.domain.model.webclient.technology.Technology;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CapabilityListTechnology {
    private Long id;
    private String name;
    private List<Technology> technologies;
}

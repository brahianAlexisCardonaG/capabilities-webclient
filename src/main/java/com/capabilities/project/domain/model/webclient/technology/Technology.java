package com.capabilities.project.domain.model.client.technology;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Technology {
    private Long id;
    private String name;
    private String capabilityName;
}

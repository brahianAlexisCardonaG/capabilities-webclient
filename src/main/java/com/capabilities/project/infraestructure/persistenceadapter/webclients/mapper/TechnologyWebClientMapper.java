package com.capabilities.project.infraestructure.persistenceadapter.webclients.mapper;

import com.capabilities.project.domain.model.webclient.technology.Technology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiListTechnology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiMapTechnology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiTechnologyMessage;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologyResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api.ApiListTechnologyResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api.ApiMapTechnologyResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api.ApiTechnologyMessageResponse;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TechnologyWebClientMapper {
    ApiListTechnology toApiListTechnology(ApiListTechnologyResponse apiListTechnologyResponse);
    ApiMapTechnology toApiMapTechnology(ApiMapTechnologyResponse apiMapTechnologyResponse);
    ApiTechnologyMessage toApiTechnologyMessage(ApiTechnologyMessageResponse apiTechnologyMessageResponse);

    Technology toTechnology(TechnologyResponse technologyResponse);
    default Map<String, List<Technology>> map(Map<String, List<TechnologyResponse>> technologyResponseMap) {
        if (technologyResponseMap == null) {
            return null;
        }
        return technologyResponseMap.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()
                                .stream()
                                .map(this::toTechnology)
                                .collect(Collectors.toList())
                ));
    }
}

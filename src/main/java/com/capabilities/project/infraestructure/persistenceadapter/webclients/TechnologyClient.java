package com.capabilities.project.infraestructure.persistenceadapter.webclients;

import com.capabilities.project.domain.model.webclient.technology.api.ApiListTechnology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiMapTechnology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiTechnologyMessage;
import com.capabilities.project.domain.spi.TechnologyWebClientPort;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.error.ErrorsWebClient;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.mapper.TechnologyWebClientMapper;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api.ApiListTechnologyResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api.ApiMapTechnologyResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api.ApiTechnologyMessageResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.util.SendTokenWebClient;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TechnologyClient implements TechnologyWebClientPort {
    private final WebClient webClient;
    private final TechnologyWebClientMapper technologyWebClientMapper;

    public TechnologyClient(WebClient.Builder builder,
                            SendTokenWebClient sendTokenWebClient,
                            TechnologyWebClientMapper technologyWebClientMapper) {
        this.webClient = builder.baseUrl("http://localhost:8081")
                .filter(sendTokenWebClient.authHeaderFilter())
                .build();
        this.technologyWebClientMapper = technologyWebClientMapper;
    }

    @Override
    public Mono<ApiMapTechnology> getTechnologiesByCapabilityIds(List<Long> capabilityIds) {
        String url = "/api/v1/technology/by-capabilities-ids";

        String idsParam = capabilityIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(url)
                        .queryParam("capabilityIds", idsParam)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ErrorsWebClient
                        .handleError(response.bodyToMono(String.class)))
                .bodyToMono(ApiMapTechnologyResponse.class)
                .map(technologyWebClientMapper::toApiMapTechnology);
    }

    @Override
    public Mono<ApiTechnologyMessage> saveRelateTechnologiesCapabilities(Long capabilityId, List<Long> technologyIds) {

        String url = "/api/v1/technology-capability";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("capabilityId", capabilityId);
        requestBody.put("technologyIds", technologyIds);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ErrorsWebClient
                        .handleError(response
                                .bodyToMono(String.class)))
                .bodyToMono(ApiTechnologyMessageResponse.class)
                .map(technologyWebClientMapper::toApiTechnologyMessage);
    }

    @Override
    public Mono<ApiListTechnology> getTechnologiesByIds(List<Long> technologyIds) {

        String url = "/api/v1/technology";
        String idsParam = technologyIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url).queryParam("ids", idsParam).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ErrorsWebClient
                        .handleError(response
                                .bodyToMono(String.class)))
                .bodyToMono(ApiListTechnologyResponse.class)
                .map(technologyWebClientMapper::toApiListTechnology);
    }

}

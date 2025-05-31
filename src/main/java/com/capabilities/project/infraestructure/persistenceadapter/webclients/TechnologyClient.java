package com.capabilities.project.infraestructure.persistenceadapter.webclients;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.ProcessorException;
import com.capabilities.project.domain.spi.TechnologyWebClientPort;
import com.capabilities.project.infraestructure.entrypoints.util.error.ErrorDto;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.error.ErrorsWebClient;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologiesMessageResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologyCapabilityResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public TechnologyClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://localhost:8081").build();
    }

    @Override
    public Mono<TechnologyCapabilityResponse> getTechnologiesByCapabilityIds(List<Long> capabilityIds) {
        String url = "/technology/by-capabilities-ids";
        Map<String, List<Long>> requestBody = new HashMap<>();
        requestBody.put("capabilityIds", capabilityIds);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        ErrorsWebClient.handleError(response.bodyToMono(String.class)))
                .bodyToMono(TechnologyCapabilityResponse.class);
    }

    @Override
    public Mono<TechnologyCapabilityResponse> saveRelateTechnologiesCapabilities(Long capabilityId,
                                                                                 List<Long> technologyIds) {

        String url = "/technology-capability";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("capabilityId", capabilityId);
        requestBody.put("technologyIds", technologyIds);

        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        ErrorsWebClient.handleError(response.bodyToMono(String.class)))
                .bodyToMono(TechnologyCapabilityResponse.class);
    }

    @Override
    public Mono<TechnologiesMessageResponse> getTechnologiesByIds(List<Long> technologyIds) {

        String url = "/technology";
        String idsParam = technologyIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(url)
                        .queryParam("ids", idsParam)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        ErrorsWebClient.handleError(response.bodyToMono(String.class)))
                .bodyToMono(TechnologiesMessageResponse.class);
    }

}

package com.capabilities.project.infraestructure.persistenceadapter.webclients;

import com.capabilities.project.domain.spi.TechnologyWebClientPort;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.error.ErrorsWebClient;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologiesMessageResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.ApiCapabilityTechnologyResponse;
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

    public TechnologyClient(WebClient.Builder builder,SendTokenWebClient sendTokenWebClient) {
        this.webClient = builder.baseUrl("http://localhost:8081")
                .filter(sendTokenWebClient.authHeaderFilter())
                .build();
    }

    @Override
    public Mono<ApiCapabilityTechnologyResponse> getTechnologiesByCapabilityIds(List<Long> capabilityIds) {
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
                .bodyToMono(ApiCapabilityTechnologyResponse.class);
    }

    @Override
    public Mono<ApiCapabilityTechnologyResponse> saveRelateTechnologiesCapabilities(Long capabilityId, List<Long> technologyIds) {

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
                .bodyToMono(ApiCapabilityTechnologyResponse.class);
    }

    @Override
    public Mono<TechnologiesMessageResponse> getTechnologiesByIds(List<Long> technologyIds) {

        String url = "/api/v1/technology";
        String idsParam = technologyIds.stream().map(String::valueOf).collect(Collectors.joining(","));

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(url).queryParam("ids", idsParam).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> ErrorsWebClient
                        .handleError(response
                                .bodyToMono(String.class)))
                .bodyToMono(TechnologiesMessageResponse.class);
    }

}

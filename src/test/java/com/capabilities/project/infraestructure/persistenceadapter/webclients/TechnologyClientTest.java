package com.capabilities.project.infraestructure.persistenceadapter.webclients;

import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.ApiCapabilityTechnologyResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologiesMessageResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.util.SendTokenWebClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TechnologyClientTest {
    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    // Mocks para el chain de GET
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    // Mocks para el chain de POST
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;


    private TechnologyClient technologyClient;


    @BeforeEach
    public void setUp() {
        // Configuramos el builder para que al invocar baseUrl(...) y build() retorne el webClient mockeado.
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.filter(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        SendTokenWebClient sendTokenWebClient = new SendTokenWebClient();
        technologyClient = new TechnologyClient(webClientBuilder,sendTokenWebClient);
    }

    @Test
    public void testGetTechnologiesByCapabilityIds() {
        // Creamos una respuesta dummy para simular lo que vendría del servidor.
        ApiCapabilityTechnologyResponse dummyResponse = new ApiCapabilityTechnologyResponse();
        List<Long> capabilityIds = List.of(1L, 2L);

        // Simulamos el chain de llamados para un GET:
        // webClient.get() -> .uri() -> .retrieve() -> .onStatus(...) -> .bodyToMono(...)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ApiCapabilityTechnologyResponse.class))
                .thenReturn(Mono.just(dummyResponse));

        Mono<ApiCapabilityTechnologyResponse> result = technologyClient.getTechnologiesByCapabilityIds(capabilityIds);

        StepVerifier.create(result)
                .expectNext(dummyResponse)
                .verifyComplete();

        // Verificamos que se hayan invocado los métodos encadenados.
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(ApiCapabilityTechnologyResponse.class);
    }

    @Test
    public void testSaveRelateTechnologiesCapabilities() {
        ApiCapabilityTechnologyResponse dummyResponse = new ApiCapabilityTechnologyResponse();
        Long capabilityId = 123L;
        List<Long> technologyIds = List.of(10L, 20L);

        // El endpoint que se utiliza en el POST.
        String expectedUrl = "/api/v1/technology-capability";

        // Construimos el request body esperado.
        Map<String, Object> expectedRequestBody = new HashMap<>();
        expectedRequestBody.put("capabilityId", capabilityId);
        expectedRequestBody.put("technologyIds", technologyIds);

        // Simulamos el chain para un POST:
        // webClient.post() -> .uri(url) -> .contentType(...) -> .bodyValue(requestBody) -> .retrieve() -> .onStatus(...) -> .bodyToMono(...)
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(expectedUrl)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(expectedRequestBody)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ApiCapabilityTechnologyResponse.class))
                .thenReturn(Mono.just(dummyResponse));

        Mono<ApiCapabilityTechnologyResponse> result = technologyClient.saveRelateTechnologiesCapabilities(capabilityId, technologyIds);

        StepVerifier.create(result)
                .expectNext(dummyResponse)
                .verifyComplete();

        // Verificamos llamados durante el chain del POST.
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(expectedUrl);
        verify(requestBodyUriSpec).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodySpec).bodyValue(expectedRequestBody);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(ApiCapabilityTechnologyResponse.class);
    }

    @Test
    public void testGetTechnologiesByIds() {
        TechnologiesMessageResponse dummyResponse = new TechnologiesMessageResponse();
        List<Long> technologyIds = List.of(5L, 6L);

        // Simulamos el chain para otro GET similar:
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(TechnologiesMessageResponse.class))
                .thenReturn(Mono.just(dummyResponse));

        Mono<TechnologiesMessageResponse> result = technologyClient.getTechnologiesByIds(technologyIds);

        StepVerifier.create(result)
                .expectNext(dummyResponse)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(TechnologiesMessageResponse.class);
    }
}

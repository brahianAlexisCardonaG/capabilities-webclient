package com.capabilities.project.infraestructure.persistenceadapter.webclients;

import com.capabilities.project.domain.model.webclient.technology.api.ApiListTechnology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiMapTechnology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiTechnologyMessage;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.mapper.TechnologyWebClientMapper;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api.ApiListTechnologyResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api.ApiMapTechnologyResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.api.ApiTechnologyMessageResponse;
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

import java.time.LocalDate;
import java.util.ArrayList;
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

    @Mock
    private TechnologyWebClientMapper technologyWebClientMapper;

    private TechnologyClient technologyClient;


    @BeforeEach
    public void setUp() {
        // Configuramos el builder para que al invocar baseUrl(...) y build() retorne el webClient mockeado.
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.filter(any())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        SendTokenWebClient sendTokenWebClient = new SendTokenWebClient();
        technologyClient = new TechnologyClient(webClientBuilder,
                sendTokenWebClient,
                technologyWebClientMapper);
    }

    @Test
    public void testGetTechnologiesByCapabilityIds() {
        // Dummy de respuesta del WebClient (antes de mappear)
        ApiMapTechnologyResponse dummyResponse = ApiMapTechnologyResponse.builder()
                .code("200")
                .message("OK")
                .date(LocalDate.now().toString())
                .data(new HashMap<>()) // O bien, poblar con datos de prueba
                .build();
        // Dummy "mapeado" que se espera obtener del mapper
        ApiMapTechnology mappedResponse = ApiMapTechnology.builder()
                .code("200")
                .message("OK")
                .date(LocalDate.now().toString())
                .data(new HashMap<>())
                .build();

        // Simulamos la cadena del GET:
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ApiMapTechnologyResponse.class))
                .thenReturn(Mono.just(dummyResponse));
        // Simulamos el mapeo
        when(technologyWebClientMapper.toApiMapTechnology(dummyResponse)).thenReturn(mappedResponse);

        Mono<ApiMapTechnology> result = technologyClient.getTechnologiesByCapabilityIds(List.of(1L, 2L));

        StepVerifier.create(result)
                .expectNext(mappedResponse)
                .verifyComplete();

        // Verificamos la secuencia de invocaciones
        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(ApiMapTechnologyResponse.class);
    }

    @Test
    public void testSaveRelateTechnologiesCapabilities() {
        // Dummy de respuesta del WebClient (antes de mappear)
        ApiTechnologyMessageResponse dummyResponse = ApiTechnologyMessageResponse.builder()
                .code("201")
                .message("Created")
                .date(LocalDate.now().toString())
                .build();
        // Dummy mapeado esperado
        ApiTechnologyMessage mappedResponse = ApiTechnologyMessage.builder()
                .code("201")
                .message("Created")
                .date(LocalDate.now().toString())
                .build();

        Long capabilityId = 123L;
        List<Long> technologyIds = List.of(10L, 20L);
        String expectedUrl = "/api/v1/technology-capability";

        // Construir el request body esperado.
        Map<String, Object> expectedRequestBody = new HashMap<>();
        expectedRequestBody.put("capabilityId", capabilityId);
        expectedRequestBody.put("technologyIds", technologyIds);

        // Simulamos la cadena del POST:
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(expectedUrl)).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_JSON))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(expectedRequestBody))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ApiTechnologyMessageResponse.class))
                .thenReturn(Mono.just(dummyResponse));
        // Mapeo de la respuesta
        when(technologyWebClientMapper.toApiTechnologyMessage(dummyResponse))
                .thenReturn(mappedResponse);

        Mono<ApiTechnologyMessage> result = technologyClient.saveRelateTechnologiesCapabilities(capabilityId, technologyIds);

        StepVerifier.create(result)
                .expectNext(mappedResponse)
                .verifyComplete();

        // Verificamos las invocaciones en el chain del POST
        verify(webClient).post();
        verify(requestBodyUriSpec).uri(expectedUrl);
        verify(requestBodyUriSpec).contentType(MediaType.APPLICATION_JSON);
        verify(requestBodySpec).bodyValue(expectedRequestBody);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(ApiTechnologyMessageResponse.class);
    }

    @Test
    public void testGetTechnologiesByIds() {
        // Dummy de respuesta del WebClient (antes del mapeo)
        ApiListTechnologyResponse dummyResponse = ApiListTechnologyResponse.builder()
                .code("200")
                .message("OK")
                .date(LocalDate.now().toString())
                .data(new ArrayList<>()) // Se puede poblar con datos dummy de TechnologyResponse
                .build();
        // Dummy mapeado esperado
        ApiListTechnology mappedResponse = ApiListTechnology.builder()
                .code("200")
                .message("OK")
                .date(LocalDate.now().toString())
                .data(new ArrayList<>())
                .build();

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(Function.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ApiListTechnologyResponse.class))
                .thenReturn(Mono.just(dummyResponse));
        when(technologyWebClientMapper.toApiListTechnology(dummyResponse))
                .thenReturn(mappedResponse);

        Mono<ApiListTechnology> result = technologyClient.getTechnologiesByIds(List.of(5L, 6L));

        StepVerifier.create(result)
                .expectNext(mappedResponse)
                .verifyComplete();

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(any(Function.class));
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(ApiListTechnologyResponse.class);
    }
}

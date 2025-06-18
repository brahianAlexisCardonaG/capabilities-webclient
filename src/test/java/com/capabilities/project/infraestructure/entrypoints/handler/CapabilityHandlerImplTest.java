package com.capabilities.project.infraestructure.entrypoints.handler;

import com.capabilities.project.domain.api.BootcampCapabilityServicePort;
import com.capabilities.project.domain.api.CapabilityServicePort;
import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.domain.model.capability.Capability;
import com.capabilities.project.domain.model.capability.CapabilityListTechnology;
import com.capabilities.project.infraestructure.entrypoints.dto.BootcampCapabilityDto;
import com.capabilities.project.infraestructure.entrypoints.dto.CapabilityDto;
import com.capabilities.project.infraestructure.entrypoints.mapper.CapabilityMapper;
import com.capabilities.project.infraestructure.entrypoints.mapper.CapabilityMapperResponse;
import com.capabilities.project.infraestructure.entrypoints.util.error.ApplyErrorHandler;
import com.capabilities.project.infraestructure.entrypoints.util.validation.BootcampCapabilityValidation;
import com.capabilities.project.infraestructure.entrypoints.util.validation.ValidateRequestSave;
import com.capabilities.project.infraestructure.entrypoints.util.response.capability.CapabilityListTechnologyResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.reactive.function.server.MockServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CapabilityHandlerImplTest {

    @Mock
    private CapabilityServicePort capabilityServicePort;

    @Mock
    private CapabilityMapper capabilityMapper;

    @Mock
    private ValidateRequestSave validateRequestSave;

    @Mock
    private ApplyErrorHandler applyErrorHandler;

    @Mock
    private BootcampCapabilityValidation bootcampCapabilityValidation;

    @Mock
    private BootcampCapabilityServicePort bootcampCapabilityServicePort;

    @Mock
    private CapabilityMapperResponse capabilityMapperResponse;

    private CapabilityHandlerImpl handler;

    @BeforeEach
    void setUp() {
        handler = new CapabilityHandlerImpl(
                capabilityServicePort,
                capabilityMapper,
                validateRequestSave,
                applyErrorHandler,
                bootcampCapabilityValidation,
                bootcampCapabilityServicePort,
                capabilityMapperResponse
        );
    }

    @Test
    void getTechnologiesByCapabilitiesIds_shouldReturnOk() {
        // Construir request con query params: capabilityIds, order, rows y skip.
        ServerRequest request = MockServerRequest.builder()
                .queryParam("capabilityIds", "1,2")
                .queryParam("order", "desc")
                .queryParam("rows", "5")
                .queryParam("skip", "2")
                .build();

        // Simular la respuesta del service.
        // Se espera que los IDs se transformen a List<Long> = [1, 2]
        List<CapabilityListTechnology> serviceList = List.of(new CapabilityListTechnology());
        when(capabilityServicePort.findTechnologiesByIdCapabilitiesModel(List.of(1L, 2L), "desc", 2, 5))
                .thenReturn(Mono.just(serviceList));

        // Simula la transformación (mapeo) de cada elemento.
        CapabilityListTechnologyResponse dummyResponse = new CapabilityListTechnologyResponse();
        when(capabilityMapperResponse.toCapabilityListTechnologiesResponse(any()))
                .thenReturn(dummyResponse);

        // Simula que el handler de errores retorne el flujo original sin modificar.
        when(applyErrorHandler.applyErrorHandling(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = handler.getTechnologiesByCapabilitiesIds(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    // Se espera un status OK
                    assertEquals(HttpStatus.OK, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void createCapabilityRelateTechnologies_shouldReturnCreated() {
        // 1) Preparar datos de entrada
        CapabilityDto capabilityDto = new CapabilityDto();
        Capability capability = new Capability();
        List<Capability> capabilityList = List.of(capability);
        List<CapabilityListTechnology> savedList = List.of(new CapabilityListTechnology());

        // 2) Request simulado
        ServerRequest request = MockServerRequest.builder().build();

        // 3) Stubbing para cada paso del flujo
        when(validateRequestSave.validateAndMapRequest(any(ServerRequest.class)))
                .thenReturn(Flux.just(capabilityDto));

        when(capabilityMapper.toCapability(capabilityDto))
                .thenReturn(capability);

        // ← Aquí indicamos que matchee la firma Flux<Capability>
        when(capabilityServicePort.saveCapabilityTechnology(capabilityList))
                .thenReturn(Mono.just(savedList));

        lenient().when(applyErrorHandler.applyErrorHandling(any()))
                .thenAnswer(inv -> inv.getArgument(0));


        // 4) Invocamos el handler
        Mono<ServerResponse> responseMono = handler.createCapabilityRelateTechnologies(request);

        // 5) Verificamos que devuelva CREATED
        StepVerifier.create(responseMono)
                .assertNext(response -> assertEquals(HttpStatus.CREATED, response.statusCode()))
                .verifyComplete();
    }

    @Test
    void createBootcampCapability_shouldReturnCreated() {
        // Crear un BootcampCapabilityDto con datos válidos.
        BootcampCapabilityDto dto = new BootcampCapabilityDto();
        dto.setBootcampId(10L);
        dto.setCapabilityIds(List.of(1L, 2L));

        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(BootcampCapabilityDto.class))
                .thenReturn(Mono.just(dto));
        when(request.bodyToMono(BootcampCapabilityDto.class)).thenReturn(Mono.just(dto));
        when(bootcampCapabilityValidation.validateDuplicateIds(dto)).thenReturn(Mono.just(dto));
        when(bootcampCapabilityValidation.validateFieldNotNullOrBlank(dto)).thenReturn(Mono.just(dto));
        // Simular que el service de bootcamp retorna Mono.empty() (pues solo nos interesa la cadena del flujo).
        when(bootcampCapabilityServicePort.saveBootcampCapabilities(dto.getBootcampId(), dto.getCapabilityIds()))
                .thenReturn(Mono.empty());
        when(applyErrorHandler.applyErrorHandling(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = handler.createBootcampCapability(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    // Se espera un status CREATED
                    assertEquals(HttpStatus.CREATED, response.statusCode());
                })
                .verifyComplete();
    }

    @Test
    void createBootcampCapability_invalidParameters_shouldReturnError() {
        // Crear un BootcampCapabilityDto con parámetros inválidos (por ejemplo, bootcampId nulo y lista vacía en capabilityIds).
        BootcampCapabilityDto dto = new BootcampCapabilityDto();
        dto.setBootcampId(null);
        dto.setCapabilityIds(List.of());

        ServerRequest request = mock(ServerRequest.class);
        when(request.bodyToMono(BootcampCapabilityDto.class))
                .thenReturn(Mono.just(dto));

        when(bootcampCapabilityValidation.validateDuplicateIds(dto))
                .thenReturn(Mono.just(dto));
        when(bootcampCapabilityValidation.validateFieldNotNullOrBlank(dto))
                .thenReturn(Mono.error(new BusinessException(TechnicalMessage.INVALID_PARAMETERS)));


        when(applyErrorHandler.applyErrorHandling(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = handler.createBootcampCapability(request);

        StepVerifier.create(responseMono)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                throwable.getMessage().equals(TechnicalMessage.INVALID_PARAMETERS.getMessage())
                )
                .verify();
    }

    @Test
    void getCapabilitiesListByBootcampIds_shouldReturnOk() {
        // Construir request con query param "bootcampIds" separados por coma.
        ServerRequest request = MockServerRequest.builder()
                .queryParam("bootcampIds", "10,20")
                .build();

        // Simular que para cada bootcampId se retorna una lista (dummy) de capacidades.
        List<Capability> capabilitiesFor10 = List.of(new Capability());
        List<Capability> capabilitiesFor20 = List.of(new Capability());

        when(bootcampCapabilityServicePort.findCapabilitiesByBootcamp(10L))
                .thenReturn(Mono.just(capabilitiesFor10));
        when(bootcampCapabilityServicePort.findCapabilitiesByBootcamp(20L))
                .thenReturn(Mono.just(capabilitiesFor20));
        // Simular el mapeo de cada capacidad a su DTO.
        when(capabilityMapper.toCapabilityDto(any())).thenReturn(new CapabilityDto());
        when(applyErrorHandler.applyErrorHandling(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<ServerResponse> responseMono = handler.getCapabilitiesListByBootcampIds(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();
    }

    @Test
    void deleteBootcampsCapabilities_shouldReturnOk() {
        // Simular parámetros de consulta para la eliminación
        ServerRequest request = MockServerRequest.builder()
                .queryParam("capabilityIds", "1,2,3")
                .build();

        // Simular comportamiento del servicio
        when(bootcampCapabilityServicePort.deleteBootcampsCapabilities(List.of(1L, 2L, 3L)))
                .thenReturn(Mono.empty());

        // Simular el manejador de errores para que devuelva el flujo original
        when(applyErrorHandler.applyErrorHandling(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Ejecutar el método del handler
        Mono<ServerResponse> responseMono = handler.deleteBootcampsCapabilities(request);

        // Verificar que el resultado es HTTP OK
        StepVerifier.create(responseMono)
                .assertNext(response -> assertEquals(HttpStatus.OK, response.statusCode()))
                .verifyComplete();
    }
}

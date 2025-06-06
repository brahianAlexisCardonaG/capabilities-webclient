package com.capabilities.project.domain.usecase.capability;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.domain.model.client.technology.CapabilityListTechnology;
import com.capabilities.project.domain.model.client.technology.Technology;
import com.capabilities.project.domain.spi.CapabilityPersistencePort;
import com.capabilities.project.domain.spi.TechnologyWebClientPort;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.mapper.TechnologyMapper;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.ApiCapabilityTechnologyResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologiesMessageResponse;
import com.capabilities.project.infraestructure.persistenceadapter.webclients.response.TechnologyResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CapabilityUseCaseTest {
    @Mock
    private CapabilityPersistencePort capabilityPersistencePort;

    @Mock
    private TechnologyWebClientPort technologyWebClientPort;

    @Mock
    private TechnologyMapper technologyMapper;

    @Mock
    private TransactionalOperator transactionalOperator;

    @InjectMocks
    private CapabilityUseCase capabilityUseCase;

    @Test
    void getCapabilityByIds_shouldReturnCapabilities_whenExist() {
        List<Long> capabilityIds = List.of(1L, 2L);
        Capability capability1 = new Capability();
        capability1.setId(1L);
        Capability capability2 = new Capability();
        capability2.setId(2L);

        when(capabilityPersistencePort.findByIds(capabilityIds)).thenReturn(Flux.just(capability1, capability2));

        StepVerifier.create(capabilityUseCase.getCapabilityByIds(capabilityIds))
                .expectNext(capability1, capability2)
                .verifyComplete();

        verify(capabilityPersistencePort, times(1)).findByIds(capabilityIds);
    }

    @Test
    void getCapabilityByIds_shouldThrowException_whenCapabilitiesNotExist() {
        List<Long> capabilityIds = List.of(3L, 4L);

        when(capabilityPersistencePort.findByIds(capabilityIds)).thenReturn(Flux.empty());

        StepVerifier.create(capabilityUseCase.getCapabilityByIds(capabilityIds))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                throwable.getMessage().equals(TechnicalMessage.CAPABILITIES_NOT_EXISTS.getMessage()))
                .verify();

        verify(capabilityPersistencePort, times(1)).findByIds(capabilityIds);
    }


    @Test
    void findTechnologiesByIdCapabilitiesModel_shouldReturnPaginatedList_onValidResponse() {
        // Datos de entrada
        List<Long> capabilityIds = List.of(1L, 2L);
        Capability cap1 = new Capability();
        cap1.setId(1L);
        cap1.setName("Capability 1");
        Capability cap2 = new Capability();
        cap2.setId(2L);
        cap2.setName("Capability 2");
        List<Capability> persistedCapabilities = List.of(cap1, cap2);

        when(capabilityPersistencePort.findByAllIds(capabilityIds))
                .thenReturn(Mono.just(persistedCapabilities));

        TechnologyResponse techResp1 = new TechnologyResponse();
        techResp1.setId(101L);
        techResp1.setName("Tech 1");
        TechnologyResponse techResp2 = new TechnologyResponse();
        techResp2.setId(102L);
        techResp2.setName("Tech 2");

        ApiCapabilityTechnologyResponse technologyResponse = ApiCapabilityTechnologyResponse.builder().build();
        Map<String, List<TechnologyResponse>> data = new HashMap<>();
        data.put("1", List.of(techResp1));
        data.put("2", List.of(techResp2));
        technologyResponse.setData(data);

        when(technologyWebClientPort.getTechnologiesByCapabilityIds(capabilityIds))
                .thenReturn(Mono.just(technologyResponse));

        Technology tech1 = Technology.builder()
                .id(101L)
                .name("Tech 1")
                .capabilityName("Capability 1")
                .build();
        Technology tech2 = Technology.builder()
                .id(102L)
                .name("Tech 2")
                .capabilityName("Capability 2")
                .build();
        when(technologyMapper.toDomain(eq(techResp1), eq("Capability 1"))).thenReturn(tech1);
        when(technologyMapper.toDomain(eq(techResp2), eq("Capability 2"))).thenReturn(tech2);

        Mono<List<CapabilityListTechnology>> result = capabilityUseCase
                .findTechnologiesByIdCapabilitiesModel(capabilityIds, "asc", 0, 10);

        // Validación con StepVerifier
        StepVerifier.create(result)
                .assertNext(list -> {
                    assertEquals(2, list.size());

                    CapabilityListTechnology clt1 = list.get(0);
                    CapabilityListTechnology clt2 = list.get(1);

                    assertEquals(1L, clt1.getId());
                    assertEquals("Capability 1", clt1.getName());
                    assertEquals(1, clt1.getTechnologies().size());
                    assertEquals(tech1, clt1.getTechnologies().get(0));

                    assertEquals(2L, clt2.getId());
                    assertEquals("Capability 2", clt2.getName());
                    assertEquals(1, clt2.getTechnologies().size());
                    assertEquals(tech2, clt2.getTechnologies().get(0));
                })
                .verifyComplete();

        verify(capabilityPersistencePort, times(1)).findByAllIds(capabilityIds);
        verify(technologyWebClientPort, times(1)).getTechnologiesByCapabilityIds(capabilityIds);
        verify(technologyMapper, times(1)).toDomain(techResp1, "Capability 1");
        verify(technologyMapper, times(1)).toDomain(techResp2, "Capability 2");

    }

    @Test
    void findTechnologiesByIdCapabilitiesModel_shouldReturnError_whenCapabilityNotExists() {
        // Datos de entrada: Se espera encontrar 3 capabilities, pero solo se retornarán 2.
        List<Long> capabilityIds = List.of(1L, 2L, 3L);
        Capability cap1 = new Capability();
        cap1.setId(1L);
        cap1.setName("Capability 1");
        Capability cap2 = new Capability();
        cap2.setId(2L);
        cap2.setName("Capability 2");
        when(capabilityPersistencePort.findByAllIds(capabilityIds))
                .thenReturn(Mono.just(List.of(cap1, cap2)));

        // Llamada al método a testear
        Mono<List<CapabilityListTechnology>> result = capabilityUseCase
                .findTechnologiesByIdCapabilitiesModel(capabilityIds, "asc", 0, 10);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                throwable.getMessage().equals(TechnicalMessage.CAPABILITY_NOT_EXISTS.getMessage()))
                .verify();

        verify(capabilityPersistencePort, times(1)).findByAllIds(capabilityIds);
        verifyNoInteractions(technologyWebClientPort);
    }

    @Test
    void saveCapabilityTechnology_shouldSaveAndReturnList_onValidPayload() {
        // 1. Preparar la capability de entrada (sin ID asignado)
        Capability inputCapability = new Capability();
        inputCapability.setName("New Capability");
        inputCapability.setTechnologyIds(List.of(100L));
        // Opcionalmente, puedes asignar description u otros campos

        Flux<Capability> capabilityFlux = Flux.just(inputCapability);

        // 2. Verificar que la capability no existe (findByName retorna false)
        when(capabilityPersistencePort.findByName("New Capability"))
                .thenReturn(Mono.just(Boolean.FALSE));

        // 3. Validar que las tecnologías existen:
        // Se simula que al llamar a getTechnologiesByIds se retorna un objeto TechnologiesMessageResponse
        // con un listado no vacío en el campo data.
        TechnologyResponse techResponseForIds = TechnologyResponse.builder()
                .id(100L)
                .name("Tech 100")
                .build();
        TechnologiesMessageResponse techsMessageResponse = TechnologiesMessageResponse.builder()
                .code("200")
                .message("OK")
                .date("2025-06-01")
                .data(List.of(techResponseForIds))
                .build();

        when(technologyWebClientPort.getTechnologiesByIds(inputCapability.getTechnologyIds()))
                .thenReturn(Mono.just(techsMessageResponse));

        // 4. Simular la persistencia: se asigna un ID a la capability guardada
        Capability savedCapability = new Capability();
        savedCapability.setId(10L);
        savedCapability.setName("New Capability");
        savedCapability.setTechnologyIds(inputCapability.getTechnologyIds());

        when(capabilityPersistencePort.save(any(Flux.class)))
                .thenReturn(Flux.just(savedCapability));

        // 5. Simular el guardado de relaciones entre la capability y sus tecnologías
        when(technologyWebClientPort.saveRelateTechnologiesCapabilities(eq(10L), eq(inputCapability.getTechnologyIds())))
                .thenReturn(Mono.empty());

        // 6. Simular la obtención de tecnologías asociadas mediante getTechnologiesByCapabilityIds:
        // Se configura el API response con un Map con clave "10" (como String) que contiene una lista de TechnologyResponse.
        TechnologyResponse techRespForMapping = TechnologyResponse.builder()
                .id(100L)
                .name("Tech 100")
                .build();

        Map<String, List<TechnologyResponse>> mapData = new HashMap<>();
        mapData.put("10", List.of(techRespForMapping));

        ApiCapabilityTechnologyResponse apiResponse = ApiCapabilityTechnologyResponse.builder()
                .code("200")
                .message("OK")
                .date("2025-06-01")
                .data(mapData)
                .build();

        when(technologyWebClientPort.getTechnologiesByCapabilityIds(List.of(10L)))
                .thenReturn(Mono.just(apiResponse));

        // 7. Simular la transformación de TechnologyResponse a Technology mediante el mapper
        Technology mappedTechnology = Technology.builder()
                .id(100L)
                .name("Tech 100")
                .capabilityName("New Capability")
                .build();

        when(technologyMapper.toDomain(eq(techRespForMapping), eq("New Capability")))
                .thenReturn(mappedTechnology);

        // 8. Configurar el TransactionalOperator para que simplemente devuelva el flujo recibido
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // 9. Invocar el método a testear
        Mono<List<CapabilityListTechnology>> result = capabilityUseCase.saveCapabilityTechnology(capabilityFlux);

        // 10. Validar la respuesta con StepVerifier
        StepVerifier.create(result)
                .assertNext(list -> {
                    // Se espera que la lista tenga un único elemento
                    assertEquals(1, list.size());
                    CapabilityListTechnology clt = list.get(0);
                    // Verifica que se propaguen correctamente el ID y el nombre de la capability
                    assertEquals(10L, clt.getId());
                    assertEquals("New Capability", clt.getName());
                    // Se valida que se hayan mapeado las tecnologías
                    assertNotNull(clt.getTechnologies());
                    assertEquals(1, clt.getTechnologies().size());
                    assertEquals(mappedTechnology, clt.getTechnologies().get(0));
                })
                .verifyComplete();

        // 11. Verificar las interacciones con los mocks
        verify(capabilityPersistencePort, times(1)).findByName("New Capability");
        verify(technologyWebClientPort, times(1))
                .getTechnologiesByIds(inputCapability.getTechnologyIds());
        verify(capabilityPersistencePort, times(1)).save(any(Flux.class));
        verify(technologyWebClientPort, times(1))
                .saveRelateTechnologiesCapabilities(eq(10L), eq(inputCapability.getTechnologyIds()));
        verify(technologyWebClientPort, times(1)).getTechnologiesByCapabilityIds(List.of(10L));
        verify(technologyMapper, times(1)).toDomain(techRespForMapping, "New Capability");
    }

    @Test
    void saveCapabilityTechnology_shouldReturnError_whenTechnologiesNotExist() {
        Capability inputCapability = new Capability();
        inputCapability.setName("New Capability");
        inputCapability.setTechnologyIds(List.of(100L));

        Flux<Capability> capabilityFlux = Flux.just(inputCapability);

        when(capabilityPersistencePort.findByName("New Capability"))
                .thenReturn(Mono.just(Boolean.FALSE));

        TechnologiesMessageResponse emptyTechResponse = TechnologiesMessageResponse.builder()
                .code("200")
                .message("OK")
                .date("2025-06-01")
                .data(List.of()) // data vacía
                .build();

        when(technologyWebClientPort.getTechnologiesByIds(inputCapability.getTechnologyIds()))
                .thenReturn(Mono.just(emptyTechResponse));

        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Mono<List<CapabilityListTechnology>> result = capabilityUseCase.saveCapabilityTechnology(capabilityFlux);

        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                throwable.getMessage().equals(TechnicalMessage.TECHNOLOGY_NOT_EXISTS.getMessage()))
                .verify();

        verify(capabilityPersistencePort, never()).save(any(Flux.class));
        verify(technologyWebClientPort, never()).saveRelateTechnologiesCapabilities(anyLong(), any());
        verify(technologyWebClientPort, never()).getTechnologiesByCapabilityIds(any());
    }

}

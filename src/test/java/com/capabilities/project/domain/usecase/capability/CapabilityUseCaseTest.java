package com.capabilities.project.domain.usecase.capability;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.domain.model.capability.Capability;
import com.capabilities.project.domain.model.capability.CapabilityListTechnology;
import com.capabilities.project.domain.model.webclient.technology.Technology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiListTechnology;
import com.capabilities.project.domain.model.webclient.technology.api.ApiMapTechnology;
import com.capabilities.project.domain.spi.CapabilityPersistencePort;
import com.capabilities.project.domain.spi.TechnologyWebClientPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.reactive.TransactionalOperator;
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

        List<Capability> expected = List.of(capability1, capability2);

        when(capabilityPersistencePort.findByIds(capabilityIds)).thenReturn(Mono.just(expected));

        StepVerifier.create(capabilityUseCase.getCapabilityByIds(capabilityIds))
                .expectNext(expected)
                .verifyComplete();

        verify(capabilityPersistencePort, times(1)).findByIds(capabilityIds);
    }

    @Test
    void getCapabilityByIds_shouldThrowException_whenCapabilitiesNotExist() {
        List<Long> capabilityIds = List.of(3L, 4L);

        when(capabilityPersistencePort.findByIds(capabilityIds)).thenReturn(Mono.empty());

        StepVerifier.create(capabilityUseCase.getCapabilityByIds(capabilityIds))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                throwable.getMessage().equals(TechnicalMessage.CAPABILITIES_NOT_EXISTS.getMessage()))
                .verify();

        verify(capabilityPersistencePort).findByIds(capabilityIds);
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

        // Preparo las tecnologías "crudas" que vienen del web client (sin capabilityName)
        Technology rawTech1 = Technology.builder()
                .id(101L)
                .name("Tech 1")
                .build();
        Technology rawTech2 = Technology.builder()
                .id(102L)
                .name("Tech 2")
                .build();

        ApiMapTechnology apiMap = ApiMapTechnology.builder().build();
        Map<String, List<Technology>> data = new HashMap<>();
        data.put("1", List.of(rawTech1));
        data.put("2", List.of(rawTech2));
        apiMap.setData(data);

        when(technologyWebClientPort.getTechnologiesByCapabilityIds(capabilityIds))
                .thenReturn(Mono.just(apiMap));

        // Ejecuto el use case
        Mono<List<CapabilityListTechnology>> result = capabilityUseCase
                .findTechnologiesByIdCapabilitiesModel(capabilityIds, "asc", 0, 10);

        // Verifico el resultado
        StepVerifier.create(result)
                .assertNext(list -> {
                    assertEquals(2, list.size());

                    CapabilityListTechnology clt1 = list.get(0);
                    CapabilityListTechnology clt2 = list.get(1);

                    // Primer capability
                    assertEquals(1L, clt1.getId());
                    assertEquals("Capability 1", clt1.getName());
                    assertEquals(1, clt1.getTechnologies().size());
                    Technology mapped1 = clt1.getTechnologies().get(0);
                    assertEquals(101L, mapped1.getId());
                    assertEquals("Tech 1", mapped1.getName());

                    // Segundo capability
                    assertEquals(2L, clt2.getId());
                    assertEquals("Capability 2", clt2.getName());
                    assertEquals(1, clt2.getTechnologies().size());
                    Technology mapped2 = clt2.getTechnologies().get(0);
                    assertEquals(102L, mapped2.getId());
                    assertEquals("Tech 2", mapped2.getName());
                })
                .verifyComplete();

        // Verifico interacciones
        verify(capabilityPersistencePort, times(1)).findByAllIds(capabilityIds);
        verify(technologyWebClientPort, times(1)).getTechnologiesByCapabilityIds(capabilityIds);
    }


    @Test
    void saveCapabilityTechnology_shouldSaveAndReturnList_onValidPayload() {
        // 1. Preparar la capability de entrada (sin ID)
        Capability inputCap = new Capability();
        inputCap.setName("New Capability");
        inputCap.setTechnologyIds(List.of(100L));
        List<Capability> capabilityList = List.of(inputCap);

        // 2. Simular que la capability no existe aún
        when(capabilityPersistencePort.findByName("New Capability"))
                .thenReturn(Mono.just(Boolean.FALSE));

        // 3. Simular validación de tecnologías existentes:
        //    getTechnologiesByIds ahora retorna ApiListTechnology con List<Technology>
        Technology rawTech = Technology.builder()
                .id(100L)
                .name("Tech 100")
                .build();

        ApiListTechnology apiListTech = ApiListTechnology.builder()
                .code("200")
                .message("OK")
                .date("2025-06-01")
                .data(List.of(rawTech))
                .build();

        when(technologyWebClientPort.getTechnologiesByIds(inputCap.getTechnologyIds()))
                .thenReturn(Mono.just(apiListTech));

        // 4. Simular persistencia: asignación de ID
        Capability savedCap = new Capability();
        savedCap.setId(10L);
        savedCap.setName("New Capability");
        savedCap.setTechnologyIds(inputCap.getTechnologyIds());

        when(capabilityPersistencePort.save(inputCap))
                .thenReturn(Mono.just(savedCap));

        // 5. Simular guardado de relación Capability–Tech
        when(technologyWebClientPort.saveRelateTechnologiesCapabilities(
                eq(10L),
                eq(inputCap.getTechnologyIds())
        )).thenReturn(Mono.empty());

        // 6. Simular consulta de las tecnologías asociadas tras guardar:
        //    getTechnologiesByCapabilityIds retorna ApiMapTechnology con Map<String,List<Technology>>
        ApiMapTechnology apiMapTech = ApiMapTechnology.builder()
                .code("200")
                .message("OK")
                .date("2025-06-01")
                .data(Map.of("10", List.of(rawTech)))
                .build();

        when(technologyWebClientPort.getTechnologiesByCapabilityIds(List.of(10L)))
                .thenReturn(Mono.just(apiMapTech));

        // 7. El TransactionalOperator debe simplemente pasar el flujo
        when(transactionalOperator.transactional(any(Mono.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // 8. Invocar el use case
        Mono<List<CapabilityListTechnology>> result = capabilityUseCase.saveCapabilityTechnology(capabilityList);

        // 9. Verificar con StepVerifier
        StepVerifier.create(result)
                .assertNext(list -> {
                    assertEquals(1, list.size());

                    CapabilityListTechnology clt = list.get(0);
                    assertEquals(10L, clt.getId());
                    assertEquals("New Capability", clt.getName());

                    List<Technology> techs = clt.getTechnologies();
                    assertNotNull(techs);
                    assertEquals(1, techs.size());

                    Technology t = techs.get(0);
                    assertEquals(100L, t.getId());
                    assertEquals("Tech 100", t.getName());
                    // En saveCapabilityTechnology no seteas capabilityName en el dom,
                    // así que si lo necesitas, deberías adaptarlo aquí o en el use case.
                })
                .verifyComplete();

        // 10. Verificar interacciones
        verify(capabilityPersistencePort).findByName("New Capability");
        verify(technologyWebClientPort).getTechnologiesByIds(inputCap.getTechnologyIds());
        verify(capabilityPersistencePort).save(inputCap);
        verify(technologyWebClientPort).saveRelateTechnologiesCapabilities(eq(10L), eq(inputCap.getTechnologyIds()));
        verify(technologyWebClientPort).getTechnologiesByCapabilityIds(List.of(10L));
    }
}

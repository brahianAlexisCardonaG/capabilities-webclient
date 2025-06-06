package com.capabilities.project.infraestructure.persistenceadapter.capability;

import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.infraestructure.persistenceadapter.capability.entity.CapabilityEntity;
import com.capabilities.project.infraestructure.persistenceadapter.capability.mapper.CapabilityEntityMapper;
import com.capabilities.project.infraestructure.persistenceadapter.capability.repository.CapabilityRespository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CapabilityPersistenceAdapterTest {
    @Mock
    private CapabilityRespository capabilityRespository;
    @Mock
    private CapabilityEntityMapper capabilityEntityMapper;
    @InjectMocks
    private CapabilityPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CapabilityPersistenceAdapter(capabilityRespository, capabilityEntityMapper);
    }

    @Test
    void findByName_ShouldReturnTrue_WhenCapabilityExists() {
        // Dado un entity que encontraría el repositorio
        CapabilityEntity entity = new CapabilityEntity();
        // Modelo esperado que devuelve el mapper
        Capability mapped = new Capability(
                1L,
                "Java",
                "Descripción de Java",
                Arrays.asList(10L, 20L)
        );

        when(capabilityRespository.findByName("Java")).thenReturn(Mono.just(entity));
        when(capabilityEntityMapper.toModel(entity)).thenReturn(mapped);

        StepVerifier.create(adapter.findByName("Java"))
                .expectNext(true)
                .verifyComplete();

        verify(capabilityRespository, times(1)).findByName("Java");
        verify(capabilityEntityMapper, times(1)).toModel(entity);
    }

    @Test
    void findByName_ShouldReturnFalse_WhenCapabilityDoesNotExist() {
        when(capabilityRespository.findByName("Java")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByName("Java"))
                .expectNext(false)
                .verifyComplete();

        verify(capabilityRespository, times(1)).findByName("Java");
        verify(capabilityEntityMapper, never()).toModel(any());
    }

    @Test
    void findByAllIds_ShouldReturnListOfCapabilities() {
        // 1) Preparamos la lista de IDs tal como la va a recibir el método
        List<Long> ids = Arrays.asList(1L, 2L);

        // 2) Creamos dos CapabilityEntity “vacías” (solo para simular que las devuelve el repositorio)
        CapabilityEntity entity1 = new CapabilityEntity();
        entity1.setId(1L);
        CapabilityEntity entity2 = new CapabilityEntity();
        entity2.setId(2L);
        // 3) Definimos los modelos que el mapper debería devolver al convertir cada entidad
        Capability cap1 = new Capability(
                1L,
                "Java",
                "Desc Java",
                Collections.singletonList(100L)
        );
        Capability cap2 = new Capability(
                2L,
                "Python",
                "Desc Python",
                Arrays.asList(200L, 300L)
        );

        // 4) Stub: cuando findAllById reciba exacta la lista `ids`, devolvemos entity1 y entity2
        when(capabilityRespository.findAllById(ids))
                .thenReturn(Flux.just(entity1, entity2));

        // 5) Stub del mapper: entity1 -> cap1, entity2 -> cap2 (en el mismo orden)
        when(capabilityEntityMapper.toModel(entity1)).thenReturn(cap1);
        when(capabilityEntityMapper.toModel(entity2)).thenReturn(cap2);

        // 6) StepVerifier: esperamos exactamente una lista [cap1, cap2]
        StepVerifier.create(adapter.findByAllIds(ids))
                .expectNext(Arrays.asList(cap1, cap2))
                .verifyComplete();

        // 7) Verificamos que se llamó al repositorio y al mapper con los argumentos correctos
        verify(capabilityRespository, times(1)).findAllById(ids);
        verify(capabilityEntityMapper, times(1)).toModel(entity1);
        verify(capabilityEntityMapper, times(1)).toModel(entity2);
    }


    @Test
    void existsById_ShouldReturnTrue_WhenCapabilityExists() {
        CapabilityEntity entity = new CapabilityEntity();
        Capability mapped = new Capability(
                1L,
                "Go",
                "Descripción Go",
                Collections.emptyList()
        );

        when(capabilityRespository.findById(1L)).thenReturn(Mono.just(entity));
        when(capabilityEntityMapper.toModel(entity)).thenReturn(mapped);

        StepVerifier.create(adapter.existsById(1L))
                .expectNext(true)
                .verifyComplete();

        verify(capabilityRespository, times(1)).findById(1L);
        verify(capabilityEntityMapper, times(1)).toModel(entity);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenCapabilityDoesNotExist() {
        when(capabilityRespository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.existsById(1L))
                .expectNext(false)
                .verifyComplete();

        verify(capabilityRespository, times(1)).findById(1L);
        verify(capabilityEntityMapper, never()).toModel(any());
    }

    @Test
    void findByIds_ShouldEmitEachCapability() {
        List<Long> ids = Arrays.asList(1L, 2L);

        CapabilityEntity entity1 = new CapabilityEntity();
        entity1.setId(1L);
        CapabilityEntity entity2 = new CapabilityEntity();
        entity2.setId(2L);
        Capability cap1 = new Capability(
                1L,
                "Kotlin",
                "Desc Kotlin",
                Arrays.asList(400L)
        );
        Capability cap2 = new Capability(
                2L,
                "Rust",
                "Desc Rust",
                Arrays.asList(500L, 600L)
        );

        when(capabilityRespository.findAllById(ids))
                .thenReturn(Flux.just(entity1, entity2));
        when(capabilityEntityMapper.toModel(entity1)).thenReturn(cap1);
        when(capabilityEntityMapper.toModel(entity2)).thenReturn(cap2);

        StepVerifier.create(adapter.findByIds(ids))
                .expectNext(cap1)
                .expectNext(cap2)
                .verifyComplete();

        verify(capabilityRespository, times(1)).findAllById(ids);
        verify(capabilityEntityMapper, times(1)).toModel(entity1);
        verify(capabilityEntityMapper, times(1)).toModel(entity2);
    }

    @Test
    void save_ShouldPersistAndReturnCapabilities() {
        // Construimos dos Capability de ejemplo
        Capability cap1 = new Capability(
                1L,
                "JavaScript",
                "Desc JS",
                Arrays.asList(700L, 800L)
        );
        Capability cap2 = new Capability(
                2L,
                "TypeScript",
                "Desc TS",
                Collections.singletonList(900L)
        );

        // Correspondientes entidades
        CapabilityEntity entity1 = new CapabilityEntity();
        entity1.setId(1L);
        CapabilityEntity entity2 = new CapabilityEntity();
        entity2.setId(2L);

        // Mockeamos el mapper: de modelo -> entidad
        when(capabilityEntityMapper.toEntity(cap1)).thenReturn(entity1);
        when(capabilityEntityMapper.toEntity(cap2)).thenReturn(entity2);

        // Cuando se guarden ambas entidades, el repositorio responde con un Flux de las mismas
        when(capabilityRespository.saveAll(Arrays.asList(entity1, entity2)))
                .thenReturn(Flux.just(entity1, entity2));

        // Y al mapear entidad -> modelo, devolvemos cap1 y cap2 otra vez
        when(capabilityEntityMapper.toModel(entity1)).thenReturn(cap1);
        when(capabilityEntityMapper.toModel(entity2)).thenReturn(cap2);

        StepVerifier.create(adapter.save(Flux.just(cap1, cap2)))
                .expectNext(cap1)
                .expectNext(cap2)
                .verifyComplete();

        verify(capabilityEntityMapper, times(1)).toEntity(cap1);
        verify(capabilityEntityMapper, times(1)).toEntity(cap2);
        verify(capabilityRespository, times(1)).saveAll(Arrays.asList(entity1, entity2));
        verify(capabilityEntityMapper, times(1)).toModel(entity1);
        verify(capabilityEntityMapper, times(1)).toModel(entity2);
    }


    @Test
    public void testDeleteCapabilities(){
        List<Long> capIdsToDelete = Arrays.asList(50L, 60L);

        CapabilityEntity capEntity1 = new CapabilityEntity();
        CapabilityEntity capEntity2 = new CapabilityEntity();

        when(capabilityRespository.findAllById(capIdsToDelete))
                .thenReturn(Flux.just(capEntity1, capEntity2));

        // Capturamos el argumento que se envía a deleteAll.
        ArgumentCaptor<List<CapabilityEntity>> captor = ArgumentCaptor.forClass(List.class);
        when(capabilityRespository.deleteAll(anyList())).thenReturn(Mono.empty());

        Mono<Void> result = adapter.deleteCapabilities(capIdsToDelete);

        StepVerifier.create(result)
                .verifyComplete();

        verify(capabilityRespository).deleteAll(captor.capture());
        List<CapabilityEntity> capturedList = captor.getValue();
        assertEquals(2, capturedList.size());
    }
}

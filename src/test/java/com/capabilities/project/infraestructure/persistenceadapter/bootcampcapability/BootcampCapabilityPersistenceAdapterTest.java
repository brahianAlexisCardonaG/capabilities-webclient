package com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability;

import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.entity.BootcampCapabilityEntity;
import com.capabilities.project.infraestructure.persistenceadapter.bootcampcapability.repository.BootcampCapabilityRepository;
import com.capabilities.project.infraestructure.persistenceadapter.capability.entity.CapabilityEntity;
import com.capabilities.project.infraestructure.persistenceadapter.capability.mapper.CapabilityEntityMapper;
import com.capabilities.project.infraestructure.persistenceadapter.capability.repository.CapabilityRespository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BootcampCapabilityPersistenceAdapterTest {

    @Mock
    private BootcampCapabilityRepository bootcampCapabilityRepository;

    @Mock
    private CapabilityRespository capabilityRespository;

    @Mock
    private CapabilityEntityMapper capabilityEntityMapper;

    @InjectMocks
    private BootcampCapabilityPersistenceAdapter adapter;

    @BeforeEach
    void setUp() {
        // La inicialización se realiza mediante @InjectMocks
    }

    @Test
    void findCapabilitiesByBootcamp_ShouldReturnListOfCapabilityIds() {
        Long bootcampId = 10L;
        // Simulamos dos relaciones existentes para un bootcamp
        BootcampCapabilityEntity entity1 = new BootcampCapabilityEntity(1L, bootcampId, 100L);
        BootcampCapabilityEntity entity2 = new BootcampCapabilityEntity(2L, bootcampId, 200L);

        when(bootcampCapabilityRepository.findByIdBootcamp(bootcampId))
                .thenReturn(Flux.just(entity1, entity2));

        StepVerifier.create(adapter.findCapabilitiesByBootcamp(bootcampId))
                .expectNext(Arrays.asList(100L, 200L))
                .verifyComplete();

        verify(bootcampCapabilityRepository, times(1)).findByIdBootcamp(bootcampId);
    }

    @Test
    void saveRelations_ShouldPersistRelations() {
        Long bootcampId = 10L;
        List<Long> capabilityIds = Arrays.asList(100L, 200L, 300L);

        // Las instancias esperadas creadas a partir de los capabilityIds
        BootcampCapabilityEntity expectedEntity1 = new BootcampCapabilityEntity(null, bootcampId, 100L);
        BootcampCapabilityEntity expectedEntity2 = new BootcampCapabilityEntity(null, bootcampId, 200L);
        BootcampCapabilityEntity expectedEntity3 = new BootcampCapabilityEntity(null, bootcampId, 300L);
        List<BootcampCapabilityEntity> expectedEntities = Arrays.asList(expectedEntity1, expectedEntity2, expectedEntity3);

        when(bootcampCapabilityRepository.saveAll(anyList()))
                .thenReturn(Flux.fromIterable(expectedEntities));

        StepVerifier.create(adapter.saveRelations(bootcampId, capabilityIds))
                .verifyComplete();

        // Verificamos que se llamó a saveAll con la lista correcta
        verify(bootcampCapabilityRepository, times(1))
                .saveAll(argThat(new ArgumentMatcher<List<BootcampCapabilityEntity>>() {
                    @Override
                    public boolean matches(List<BootcampCapabilityEntity> list) {
                        return list.equals(expectedEntities);
                    }
                }));
    }

    @Test
    void findCapabilitiesListByBootcamp_ShouldReturnListOfCapabilities() {
        Long bootcampId = 10L;
        // Simulamos dos entidades de relación para el bootcamp
        BootcampCapabilityEntity bpEntity1 = new BootcampCapabilityEntity(1L, bootcampId, 100L);
        BootcampCapabilityEntity bpEntity2 = new BootcampCapabilityEntity(2L, bootcampId, 200L);

        when(bootcampCapabilityRepository.findByIdBootcamp(bootcampId))
                .thenReturn(Flux.just(bpEntity1, bpEntity2));

        CapabilityEntity dummyCapEntity1 = new CapabilityEntity();
        dummyCapEntity1.setId(100L);
        dummyCapEntity1.setName("Java");
        dummyCapEntity1.setDescription("Desc Java");

        CapabilityEntity dummyCapEntity2 = new CapabilityEntity();
        dummyCapEntity2.setId(200L);
        dummyCapEntity2.setName("Python");
        dummyCapEntity2.setDescription("Desc Python");

        when(capabilityRespository.findAllById(Arrays.asList(100L, 200L)))
                .thenReturn(Flux.just(dummyCapEntity1, dummyCapEntity2));

        // Definimos las conversiones esperadas a modelo de dominio
        Capability cap1 = new Capability(100L, "Java", "Desc Java", Collections.singletonList(10L));
        Capability cap2 = new Capability(200L, "Python", "Desc Python", Collections.singletonList(20L));
        when(capabilityEntityMapper.toModel(dummyCapEntity1)).thenReturn(cap1);
        when(capabilityEntityMapper.toModel(dummyCapEntity2)).thenReturn(cap2);

        StepVerifier.create(adapter.findCapabilitiesListByBootcamp(bootcampId))
                .expectNext(Arrays.asList(cap1, cap2))
                .verifyComplete();

        verify(bootcampCapabilityRepository, times(1)).findByIdBootcamp(bootcampId);
        verify(capabilityRespository, times(1)).findAllById(Arrays.asList(100L, 200L));
        verify(capabilityEntityMapper, times(1)).toModel(dummyCapEntity1);
        verify(capabilityEntityMapper, times(1)).toModel(dummyCapEntity2);
    }

    @Test
    void existsBootcampById_ShouldReturnTrue_WhenBootcampExists() {
        Long bootcampId = 10L;
        BootcampCapabilityEntity entity = new BootcampCapabilityEntity(1L, bootcampId, 100L);

        when(bootcampCapabilityRepository.findByIdBootcamp(bootcampId))
                .thenReturn(Flux.just(entity));

        StepVerifier.create(adapter.existsBootcampById(bootcampId))
                .expectNext(true)
                .verifyComplete();

        verify(bootcampCapabilityRepository, times(1)).findByIdBootcamp(bootcampId);
    }

    @Test
    void existsBootcampById_ShouldReturnFalse_WhenBootcampDoesNotExist() {
        Long bootcampId = 10L;

        when(bootcampCapabilityRepository.findByIdBootcamp(bootcampId))
                .thenReturn(Flux.empty());

        StepVerifier.create(adapter.existsBootcampById(bootcampId))
                .expectNext(false)
                .verifyComplete();

        verify(bootcampCapabilityRepository, times(1)).findByIdBootcamp(bootcampId);
    }

    @Test
    void deleteCapabilitiesTechnologies_ShouldDeleteMatchingEntities() {
        // Simular IDs que serán eliminados
        List<Long> capabilityIds = List.of(1L, 2L, 3L);

        // Simular las entidades que corresponden a los IDs
        BootcampCapabilityEntity entity1 = new BootcampCapabilityEntity(1L, 100L, 1L);
        BootcampCapabilityEntity entity2 = new BootcampCapabilityEntity(2L, 100L, 2L);
        BootcampCapabilityEntity entity3 = new BootcampCapabilityEntity(3L, 100L, 3L);

        when(bootcampCapabilityRepository.findAll())
                .thenReturn(Flux.just(entity1, entity2, entity3));
        ArgumentCaptor<List<BootcampCapabilityEntity>> captor = ArgumentCaptor.forClass(List.class);
        when(bootcampCapabilityRepository.deleteAll(anyList()))
                .thenReturn(Mono.empty());

        Mono<Void> result = adapter.deleteBootcampsCapabilities(capabilityIds);

        StepVerifier.create(result)
                .verifyComplete();

        // Verificar que se llamara a `deleteAll` con las entidades correctas
        verify(bootcampCapabilityRepository)
                .deleteAll(captor.capture());
        List<BootcampCapabilityEntity> capturedList = captor.getValue();
        assertEquals(3, capturedList.size());
        capturedList.forEach(entity -> assertTrue(capabilityIds
                .contains(entity.getIdCapability())));
    }
}

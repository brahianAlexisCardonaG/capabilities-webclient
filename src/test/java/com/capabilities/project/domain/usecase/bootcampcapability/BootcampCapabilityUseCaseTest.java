package com.capabilities.project.domain.usecase.bootcampcapability;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.domain.model.Capability;
import com.capabilities.project.domain.spi.BootcampCapabilityPersistencePort;
import com.capabilities.project.domain.spi.CapabilityPersistencePort;
import com.capabilities.project.domain.usecase.bootcampcapability.util.ValidationBootcampCapability;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BootcampCapabilityUseCaseTest {
    @Mock
    private CapabilityPersistencePort capabilityPersistencePort;
    @Mock
    private BootcampCapabilityPersistencePort bootcampCapabilityPersistencePort;
    @Mock
    private ValidationBootcampCapability validationBootcampCapability;
    @InjectMocks
    private BootcampCapabilityUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BootcampCapabilityUseCase(capabilityPersistencePort, bootcampCapabilityPersistencePort, validationBootcampCapability);
    }

    @Test
    void saveBootcampCapabilities_success() {
        Long bootcampId = 1L;
        List<Long> capabilityIds = List.of(10L, 20L);

        when(capabilityPersistencePort.existsById(anyLong())).thenReturn(Mono.just(true));

        when(bootcampCapabilityPersistencePort.findCapabilitiesByBootcamp(bootcampId))
                .thenReturn(Mono.just(List.of(30L)));

        when(validationBootcampCapability.validateHasDuplicatesCapabilities(anySet(), anyList()))
                .thenReturn(Mono.empty());

        when(validationBootcampCapability.validateNumberCapabilities(anyList(), anyList()))
                .thenReturn(Mono.empty());

        when(bootcampCapabilityPersistencePort.saveRelations(bootcampId, capabilityIds))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.saveBootcampCapabilities(bootcampId, capabilityIds))
                .verifyComplete();

        verify(bootcampCapabilityPersistencePort).saveRelations(bootcampId, capabilityIds);
    }

    @Test
    void saveBootcampCapabilities_fails_whenCapabilityDoesNotExist() {
        Long bootcampId = 1L;
        List<Long> capabilityIds = List.of(10L, 20L);

        // Simula que la primera existe, la segunda no
        when(capabilityPersistencePort.existsById(10L)).thenReturn(Mono.just(true));
        when(capabilityPersistencePort.existsById(20L)).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.saveBootcampCapabilities(bootcampId, capabilityIds))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                ((BusinessException) throwable).getTechnicalMessage().equals(TechnicalMessage.CAPABILITIES_NOT_EXISTS)
                ).verify();

        verify(bootcampCapabilityPersistencePort, never()).saveRelations(anyLong(), anyList());
    }

    @Test
    void findCapabilitiesByBootcamp_success() {
        Long bootcampId = 1L;
        List<Capability> capabilities = List.of(
                new Capability(1L, "Java", "Backend language", List.of(100L)),
                new Capability(2L, "Spring", "Framework", List.of(101L))
        );

        when(bootcampCapabilityPersistencePort.existsBootcampById(bootcampId)).thenReturn(Mono.just(true));
        when(bootcampCapabilityPersistencePort.findCapabilitiesListByBootcamp(bootcampId)).thenReturn(Mono.just(capabilities));

        StepVerifier.create(useCase.findCapabilitiesByBootcamp(bootcampId))
                .expectNext(capabilities)
                .verifyComplete();
    }

    @Test
    void findCapabilitiesByBootcamp_notFound() {
        Long bootcampId = 1L;

        when(bootcampCapabilityPersistencePort.existsBootcampById(bootcampId)).thenReturn(Mono.just(false));

        StepVerifier.create(useCase.findCapabilitiesByBootcamp(bootcampId))
                .expectErrorMatches(throwable ->
                        throwable instanceof BusinessException &&
                                ((BusinessException) throwable).getTechnicalMessage().equals(TechnicalMessage.CAPABILITIES_NOT_EXISTS)
                ).verify();
    }

    @Test
    void deleteCapabilityTechnologies_shouldDeleteSuccessfully() {
        List<Long> capabilityIds = List.of(1L, 2L);
        when(bootcampCapabilityPersistencePort.findBootcampsByCapabilitiesIds(capabilityIds))
                .thenReturn(Flux.just(100L));

        // Configuramos las llamadas de borrado para que se completen sin error
        when(bootcampCapabilityPersistencePort.deleteBootcampsCapabilities(capabilityIds)).thenReturn(Mono.empty());
        when(capabilityPersistencePort.deleteCapabilities(capabilityIds)).thenReturn(Mono.empty());

        Mono<Void> result = useCase.deleteBootcampsCapabilities(capabilityIds);

        StepVerifier.create(result)
                .verifyComplete();

        // Verificamos que se haya llamado a los métodos de borrado correspondientes.
        verify(bootcampCapabilityPersistencePort).deleteBootcampsCapabilities(capabilityIds);
        verify(capabilityPersistencePort).deleteCapabilities(capabilityIds);
    }

    @Test
    void deleteCapabilityTechnologies_shouldThrowErrorIfMultipleCapabilitiesFound() {
        List<Long> capabilityIds = List.of(1L, 2L);

        when(bootcampCapabilityPersistencePort.findBootcampsByCapabilitiesIds(capabilityIds))
                .thenReturn(Flux.just(100L, 200L));

        Mono<Void> result = useCase.deleteBootcampsCapabilities(capabilityIds);

        StepVerifier.create(result)
                .expectErrorMatches(error -> error instanceof BusinessException &&
                        ((BusinessException) error)
                                .getTechnicalMessage() == TechnicalMessage
                                .BOOTCAMPS_CAPABILITIES_MORE_ONE_RELATE)
                .verify();

        // Verificamos que, en caso de error, no se invoquen los métodos de borrado.
        verify(bootcampCapabilityPersistencePort, never()).deleteBootcampsCapabilities(any());
        verify(capabilityPersistencePort, never()).deleteCapabilities(any());
    }
}

package com.capabilities.project.infraestructure.entrypoints.util.validate;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import com.capabilities.project.infraestructure.entrypoints.dto.BootcampCapabilityDto;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class BootcampCapabilityValidation {

    public Mono<BootcampCapabilityDto> validateDuplicateIds(BootcampCapabilityDto bootcampCapabilityDto) {
        Set<Long> ids = new HashSet<>();
        List<Long> duplicatedIds = bootcampCapabilityDto.getCapabilityIds().stream()
                .filter(id -> !ids.add(id)) // Si no se puede agregar al set, es duplicado
                .toList();

        if (!duplicatedIds.isEmpty()) {
            return Mono.error(new BusinessException(TechnicalMessage.CAPABILITIES_DUPLICATES_IDS));
        }

        return Mono.just(bootcampCapabilityDto);
    }

    public Mono<BootcampCapabilityDto> validateFieldNotNullOrBlank(BootcampCapabilityDto dto) {
        if (dto.getBootcampId() == null || dto.getCapabilityIds() == null || dto.getCapabilityIds().isEmpty()) {
            return Mono.error(new BusinessException(TechnicalMessage.INVALID_PARAMETERS));
        }
        return Mono.just(dto);
    }
}

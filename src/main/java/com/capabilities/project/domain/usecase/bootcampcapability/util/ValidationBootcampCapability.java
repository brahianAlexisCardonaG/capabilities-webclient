package com.capabilities.project.domain.usecase.bootcampcapability.util;

import com.capabilities.project.domain.enums.TechnicalMessage;
import com.capabilities.project.domain.exception.BusinessException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
public class ValidationBootcampCapability {

    public Mono<Void> validateNumberCapabilities(List<Long> existingCapabilities, List<Long> newCapabilities) {
        int total = existingCapabilities.size() + newCapabilities.size();

        if (total > 4) {
            return Mono.error(new BusinessException(TechnicalMessage.CAPABILITY_FOUR_ASSOCIATION));
        }

        if (total < 1) {
            return Mono.error(new BusinessException(TechnicalMessage.CAPABILITY_ONE_ASSOCIATION));
        }

        return Mono.empty();
    }

    public Mono<Void> validateHasDuplicatesCapabilities(Set<Long> existingSet, List<Long> newCapabilities) {
        boolean hasDuplicates = newCapabilities.stream().anyMatch(existingSet::contains);

        if (hasDuplicates) {
            return Mono.error(new BusinessException(TechnicalMessage.CAPABILITY_TECH_ALREADY_ASSOCIATED));
        }

        return Mono.empty();
    }
}

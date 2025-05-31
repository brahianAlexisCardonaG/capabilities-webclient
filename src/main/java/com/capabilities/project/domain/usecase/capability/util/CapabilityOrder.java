package com.capabilities.project.domain.usecase.capability.util;

import com.capabilities.project.domain.model.client.technology.Technology;

import java.util.*;
import java.util.stream.Collectors;

public class CapabilityOrder {
    public static Map<String, List<Technology>> sort(Map<String, List<Technology>> unsortedMap, String order) {
        Comparator<Map.Entry<String, List<Technology>>> comparator;

        if (order != null && order.equalsIgnoreCase("tech")) {
            // Ordenar descendente por cantidad de tecnologías
            comparator = Comparator.comparingInt(
                    (Map.Entry<String, List<Technology>> e) -> e.getValue().size()
            ).reversed();
        } else if (order != null && order.equalsIgnoreCase("desc")) {
            // Ordenar descendente alfabéticamente por el nombre
            comparator = Comparator.comparing(
                    (Map.Entry<String, List<Technology>> e) -> e.getKey(),
                    Comparator.reverseOrder()
            );
        } else {
            // Orden ascendente (por defecto) por el nombre
            comparator = Comparator.comparing(
                    (Map.Entry<String, List<Technology>> e) -> e.getKey()
            );
        }

        return unsortedMap.entrySet().stream()
                .sorted(comparator)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new  // Mantener el orden definido
                ));
    }
}

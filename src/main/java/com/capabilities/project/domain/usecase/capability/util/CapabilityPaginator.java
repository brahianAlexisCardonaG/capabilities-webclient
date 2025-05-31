package com.capabilities.project.domain.usecase.capability.util;

import com.capabilities.project.domain.model.client.technology.Technology;

import java.util.*;
import java.util.stream.Collectors;

public class CapabilityPaginator {
    public static Map<String, List<Technology>> paginate(Map<String, List<Technology>> sortedMap, int skip, int rows) {
        List<Map.Entry<String, List<Technology>>> entries = new ArrayList<>(sortedMap.entrySet());

        // Asegurarse de no exceder el tamaño de la lista
        int fromIndex = Math.min(skip, entries.size());
        int toIndex = Math.min(skip + rows, entries.size());

        return entries.subList(fromIndex, toIndex)
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new  // Mantiene el orden de inserción
                ));
    }
}

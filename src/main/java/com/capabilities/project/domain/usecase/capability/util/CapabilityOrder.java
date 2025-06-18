package com.capabilities.project.domain.usecase.capability.util;

import com.capabilities.project.domain.model.capability.CapabilityListTechnology;

import java.util.*;
import java.util.stream.Collectors;

public class CapabilityOrder {

    public static List<CapabilityListTechnology> sortList(
            List<CapabilityListTechnology> unsortedList,
            String order) {

        Comparator<CapabilityListTechnology> comparator;

        if (order != null && order.equalsIgnoreCase("tech")) {
            // Orden descendente por cantidad de tecnologías
            comparator = Comparator.comparingInt(
                    (CapabilityListTechnology c) -> c.getTechnologies().size()
            ).reversed();
        }
        else if (order != null && order.equalsIgnoreCase("desc")) {
            // Orden descendente alfabéticamente por nombre de la capacidad
            comparator = Comparator.comparing(
                    CapabilityListTechnology::getName,
                    Comparator.reverseOrder()
            );
        }
        else {
            // Orden ascendente (por defecto) alfabéticamente por nombre de la capacidad
            comparator = Comparator.comparing(
                    CapabilityListTechnology::getName
            );
        }

        return unsortedList.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

}

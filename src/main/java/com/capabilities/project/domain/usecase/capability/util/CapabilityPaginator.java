package com.capabilities.project.domain.usecase.capability.util;

import com.capabilities.project.domain.model.capability.CapabilityListTechnology;

import java.util.*;

public class CapabilityPaginator {
    public static List<CapabilityListTechnology> paginateList(List<CapabilityListTechnology> sortedList,
                                                              int skip,
                                                              int rows) {
        if (skip >= sortedList.size()) {
            return Collections.emptyList();
        }
        int endIndex = Math.min(sortedList.size(), skip + rows);
        return sortedList.subList(skip, endIndex);
    }
}

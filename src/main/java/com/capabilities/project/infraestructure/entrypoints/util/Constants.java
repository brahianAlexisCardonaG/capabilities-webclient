package com.capabilities.project.infraestructure.entrypoints.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {
    public static final String X_MESSAGE_ID = "x-message-id";
    public static final String CAPABILITY_ERROR = "Error on Capability - [ERROR]";

    public static final String PATH_GET_TECHNOLOGIES_BY_IDS_CAPABILITY
            = "/api/v1/capability/find-technologies";
    public static final String PATH_POST_CREATE_RELATE_BOOTCAMP_CAPABILITY
            = "/api/v1/capability-bootcamp";
    public static final String PATH_POST_CREATE_RELATE_CAPABILITY_TECHNOLOGY
            = "/api/v1/capability/relate-technologies";
    public static final String PATH_GET_CAPABILITIES_BY_IDS_BOOTCAMPS
            = "/api/v1/capability/by-bootcamp-ids";
    public static final String PATH_GET_CAPABILITIES_BY_IDS
            = "/api/v1/capability";
    public static final String PATH_DELETE_RELATE_BOOTCAMPS_CAPABILITIES
            = "/api/v1/capability/bootcamp/delete";
}

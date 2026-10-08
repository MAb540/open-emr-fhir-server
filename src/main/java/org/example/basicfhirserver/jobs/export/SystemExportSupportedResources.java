package org.example.basicfhirserver.jobs.export;

import lombok.Getter;

@Getter
public enum SystemExportSupportedResources {

    ORGANIZATION("Organization"),
    LOCATION("Location"),
    PRACTITIONER("Practitioner");

    private final String value;

    SystemExportSupportedResources(String value) {
        this.value = value;
    }
}

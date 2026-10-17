package org.example.basicfhirserver.service.impl.person;

import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

public record PersonSearchResult(
    List<IBaseResource> primaryResources,
    long totalElements) {}

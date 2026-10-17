package org.example.basicfhirserver.service.impl.condition;

import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

public record ConditionSearchResult(
    List<IBaseResource> primaryResources,
    List<IBaseResource> includedResources,
    long totalElements) {}

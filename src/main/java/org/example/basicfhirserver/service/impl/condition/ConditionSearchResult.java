package org.example.basicfhirserver.service.impl.condition;

import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.List;

public record ConditionSearchResult(
    List<IBaseResource> primaryResources,
    List<IBaseResource> includedResources,
    long totalElements) {}

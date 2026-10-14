package org.example.basicfhirserver.service.impl.allergyIntolerance;

import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.List;

public record AllergySearchResult(
    List<IBaseResource> primaryResources,
    List<IBaseResource> includedResources,
    long totalElements) {}

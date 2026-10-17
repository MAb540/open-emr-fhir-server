package org.example.basicfhirserver.service.impl.allergyIntolerance;

import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

public record AllergySearchResult(
    List<IBaseResource> primaryResources,
    List<IBaseResource> includedResources,
    int totalElements,
    int currentOffset) {}

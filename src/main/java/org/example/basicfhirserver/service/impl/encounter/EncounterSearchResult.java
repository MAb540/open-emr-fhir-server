package org.example.basicfhirserver.service.impl.encounter;

import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.List;

public record EncounterSearchResult(List<IBaseResource> primaryResources, long totalElements) {}

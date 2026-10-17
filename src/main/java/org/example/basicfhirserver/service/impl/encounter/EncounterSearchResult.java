package org.example.basicfhirserver.service.impl.encounter;

import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;

public record EncounterSearchResult(List<IBaseResource> primaryResources, long totalElements) {}

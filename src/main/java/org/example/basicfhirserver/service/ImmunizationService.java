package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.immunization.ImmunizationSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Immunization;

public interface ImmunizationService {
  Immunization findById(UUID uuid);

  SearchResult search(ImmunizationSearchQuery immunizationSearchQuery);
}

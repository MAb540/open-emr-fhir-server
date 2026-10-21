package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Observation;

public interface ObservationService {

  Observation findById(UUID uuid);

  SearchResult search(ObservationSearchQuery observationSearchQuery);
}

package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.location.LocationSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Location;

public interface LocationService {

  Location findById(UUID uuid);

  SearchResult search(LocationSearchQuery locationSearchQuery);
}

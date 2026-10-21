package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Practitioner;

public interface PractitionerService {

  Practitioner findById(UUID uuid);

  SearchResult search(PractitionerSearchQuery practitionerSearchQuery);
}

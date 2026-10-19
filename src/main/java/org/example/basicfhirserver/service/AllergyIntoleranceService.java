package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.AllergyIntolerance;

public interface AllergyIntoleranceService {

  AllergyIntolerance findById(UUID uuid);

  SearchResult search(AllergyIntoleranceSearchQuery searchQuery);
}

package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.service.impl.allergyIntolerance.AllergySearchResult;
import org.hl7.fhir.r4.model.AllergyIntolerance;

public interface AllergyIntoleranceService {

  AllergyIntolerance findById(UUID uuid);

  AllergySearchResult find(AllergyIntoleranceSearchQuery searchQuery);

  //  Page<AllergyDBRecord> find(AllergyIntoleranceSearchQuery allergyIntoleranceSearchQuery);

}

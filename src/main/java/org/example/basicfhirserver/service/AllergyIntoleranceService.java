package org.example.basicfhirserver.service;

import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;

import java.util.List;
import java.util.UUID;

public interface AllergyIntoleranceService {

    AllergyDBRecord findById(UUID uuid);

    List<AllergyDBRecord> find(AllergyIntoleranceSearchQuery allergyIntoleranceSearchQuery);

}

package org.example.basicfhirserver.service;

import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface AllergyIntoleranceService {

    AllergyDBRecord findById(UUID uuid);

    Page<AllergyDBRecord> find(AllergyIntoleranceSearchQuery allergyIntoleranceSearchQuery);

}

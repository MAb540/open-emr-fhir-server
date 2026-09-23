package org.example.basicfhirserver.repository.jdbc.allergy;

import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface AllergyService {

    List<AllergyDBRecord> findById(UUID uuid);

    Page<AllergyDBRecord> find(AllergyIntoleranceSearchQuery allergyIntoleranceSearchQuery);

}

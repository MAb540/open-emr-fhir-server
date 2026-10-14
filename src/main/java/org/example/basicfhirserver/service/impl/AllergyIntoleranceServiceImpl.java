package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyRepository;
import org.example.basicfhirserver.service.AllergyIntoleranceService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class AllergyIntoleranceServiceImpl implements AllergyIntoleranceService {

  private final AllergyRepository allergyRepository;

  public AllergyIntoleranceServiceImpl(AllergyRepository allergyRepository) {
    this.allergyRepository = allergyRepository;
  }

  @Override
  public AllergyDBRecord findById(UUID uuid) {
    List<AllergyDBRecord> allergyDBRecords = allergyRepository.findById(uuid);
    if (allergyDBRecords.isEmpty()) {
      throw new ResourceNotFoundException(
          "AllergyIntolerance with given ID " + uuid + " not found.");
    }
    return allergyDBRecords.get(0);
  }

  @Override
  public Page<AllergyDBRecord> find(AllergyIntoleranceSearchQuery allergyIntoleranceSearchQuery) {
    return allergyRepository.find(allergyIntoleranceSearchQuery);
  }
}

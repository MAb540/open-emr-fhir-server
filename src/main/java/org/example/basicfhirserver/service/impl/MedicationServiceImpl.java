package org.example.basicfhirserver.service.impl;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.medication.MedicationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;
import org.example.basicfhirserver.repository.jdbc.drug.DrugRepository;
import org.example.basicfhirserver.service.MedicationService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class MedicationServiceImpl implements MedicationService {

  private final DrugRepository drugRepository;

  public MedicationServiceImpl(DrugRepository drugRepository) {
    this.drugRepository = drugRepository;
  }

  @Override
  public DrugDBRecord findById(UUID uuid) {

    List<DrugDBRecord> drugDBRecords = drugRepository.findById(uuid);
    if (drugDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Medication with given ID " + uuid + " not found.");
    }

    return drugDBRecords.get(0);
  }

  @Override
  public Page<DrugDBRecord> find(MedicationSearchQuery medicationSearchQuery) {

    return drugRepository.find(medicationSearchQuery);
  }
}

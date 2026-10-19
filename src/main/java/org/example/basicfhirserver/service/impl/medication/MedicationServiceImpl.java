package org.example.basicfhirserver.service.impl.medication;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.MedicationMapper;
import org.example.basicfhirserver.query.resources.medication.MedicationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;
import org.example.basicfhirserver.repository.jdbc.drug.DrugRepository;
import org.example.basicfhirserver.service.MedicationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Medication;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class MedicationServiceImpl implements MedicationService {

  private final DrugRepository drugRepository;
  private final MedicationMapper medicationMapper;

  public MedicationServiceImpl(DrugRepository drugRepository, MedicationMapper medicationMapper) {
    this.drugRepository = drugRepository;
    this.medicationMapper = medicationMapper;
  }

  @Override
  public Medication findById(UUID uuid) {
    List<DrugDBRecord> drugDBRecords = drugRepository.findById(uuid);
    if (drugDBRecords.isEmpty()) {
      throw new ResourceNotFoundException("Medication with given ID " + uuid + " not found.");
    }
    return medicationMapper.toR4(drugDBRecords.get(0));
  }

  @Override
  public SearchResult search(MedicationSearchQuery medicationSearchQuery) {
    Page<DrugDBRecord> drugDBRecords = drugRepository.find(medicationSearchQuery);

    List<IBaseResource> primaryMedications =
        drugDBRecords.getContent().stream().<IBaseResource>map(medicationMapper::toR4).toList();

    int currentOffset =
        medicationSearchQuery.getOffset() != null ? medicationSearchQuery.getOffset() : 0;

    return new SearchResult(
        primaryMedications, List.of(), (int) drugDBRecords.getTotalElements(), currentOffset);
  }
}

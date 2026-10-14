package org.example.basicfhirserver.service.impl.allergyIntolerance;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.AllergyIntoleranceMapper;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyRepository;
import org.example.basicfhirserver.service.AllergyIntoleranceService;
import org.example.basicfhirserver.service.PatientService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public class AllergyIntoleranceServiceImpl implements AllergyIntoleranceService {

  private final AllergyRepository allergyRepository;
  private final AllergyIntoleranceMapper allergyIntoleranceMapper;
  private final PatientService patientService;
  private final LegacyPatientMapper legacyPatientMapper;

  public AllergyIntoleranceServiceImpl(
      AllergyRepository allergyRepository,
      AllergyIntoleranceMapper allergyIntoleranceMapper,
      PatientService patientService,
      LegacyPatientMapper legacyPatientMapper) {
    this.allergyRepository = allergyRepository;
    this.allergyIntoleranceMapper = allergyIntoleranceMapper;
    this.patientService = patientService;
    this.legacyPatientMapper = legacyPatientMapper;
  }

  @Override
  public AllergyIntolerance findById(UUID uuid) {
    List<AllergyDBRecord> allergyDBRecords = allergyRepository.findById(uuid);
    if (allergyDBRecords.isEmpty()) {
      throw new ResourceNotFoundException(
          "AllergyIntolerance with given ID " + uuid + " not found.");
    }
    return allergyIntoleranceMapper.toR4(allergyDBRecords.get(0));
  }

  @Override
  public AllergySearchResult find(AllergyIntoleranceSearchQuery allergyIntoleranceSearchQuery) {

    Page<AllergyDBRecord> allergyDBRecords = allergyRepository.find(allergyIntoleranceSearchQuery);
    List<IBaseResource> primaryAllergies =
        allergyDBRecords.getContent().stream()
            .<IBaseResource>map(allergyIntoleranceMapper::toR4)
            .toList();

    List<IBaseResource> includedResources = new ArrayList<>();
    Set<Include> includes = allergyIntoleranceSearchQuery.getIncludes();
    boolean includePatients =
        includes != null
            && includes.stream()
                .anyMatch(inc -> "AllergyIntolerance:patient".equals(inc.getValue()));

    if (includePatients && !allergyDBRecords.isEmpty()) {
      List<String> patientUuids =
          allergyDBRecords.getContent().stream()
              .map(record -> record.getPatientUuid().toString())
              .toList();

      PatientSearchQuery patientQuery =
          PatientSearchQuery.builder().patientId(patientUuids).build();

      Page<LegacyPatientEntity> legacyPatientEntities = patientService.find(patientQuery);
      legacyPatientEntities.getContent().stream()
          .<IBaseResource>map(legacyPatientMapper::toR4)
          .forEach(includedResources::add);
    }

    return new AllergySearchResult(
        primaryAllergies, includedResources, allergyDBRecords.getTotalElements());
  }
}

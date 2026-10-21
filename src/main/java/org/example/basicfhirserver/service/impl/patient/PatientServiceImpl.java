package org.example.basicfhirserver.service.impl.patient;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.repository.jpa.patient.PatientRepository;
import org.example.basicfhirserver.repository.jpa.patient.specs.PatientSpecifications;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.PatientService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PatientServiceImpl implements PatientService {

  private static final int DEFAULT_PAGE_SIZE = 5;

  private final PatientRepository patientRepository;
  private final LegacyPatientMapper legacyPatientMapper;
  private final ObservationService observationService;
  private final EncounterService encounterService;

  public PatientServiceImpl(
      PatientRepository patientRepository,
      LegacyPatientMapper legacyPatientMapper,
      ObservationService observationService,
      EncounterService encounterService) {
    this.patientRepository = patientRepository;
    this.legacyPatientMapper = legacyPatientMapper;
    this.observationService = observationService;
    this.encounterService = encounterService;
  }

  @Override
  public Patient findById(UUID uuid) {
    Specification<LegacyPatientEntity> spec = (root, query, cb) -> cb.equal(root.get("uuid"), uuid);
    Optional<LegacyPatientEntity> legacyPatientEntity = patientRepository.findOne(spec);

    if (legacyPatientEntity.isEmpty()) {
      throw new ResourceNotFoundException("Patient with given ID " + uuid + " not found.");
    }

    return legacyPatientMapper.toR4(legacyPatientEntity.get());
  }

  @Override
  public SearchResult search(PatientSearchQuery patientSearchQuery) {
    Specification<LegacyPatientEntity> spec = PatientSpecifications.from(patientSearchQuery);

    int limit =
        patientSearchQuery.getCount() != null ? patientSearchQuery.getCount() : DEFAULT_PAGE_SIZE;
    int offset = patientSearchQuery.getOffset() != null ? patientSearchQuery.getOffset() : 0;
    Pageable pageable = PageRequest.of(offset / limit, limit);

    Page<LegacyPatientEntity> legacyPatientEntities = patientRepository.findAll(spec, pageable);

    List<IBaseResource> primaryPatients =
        legacyPatientEntities.getContent().stream()
            .<IBaseResource>map(legacyPatientMapper::toR4)
            .toList();

    List<IBaseResource> includedResources =
        includeResources(legacyPatientEntities, patientSearchQuery.getRevIncludes());

    return new SearchResult(
        primaryPatients, includedResources, (int) legacyPatientEntities.getTotalElements(), offset);
  }

  private List<IBaseResource> includeResources(
      Page<LegacyPatientEntity> legacyPatientEntities, Set<Include> revIncludes) {
    boolean includeObservations =
        revIncludes != null
            && revIncludes.stream()
                .anyMatch(
                    inc ->
                        "Observation:patient".equals(inc.getValue())
                            || "Observation:subject".equals(inc.getValue()));

    boolean includeEncounters =
        revIncludes != null
            && revIncludes.stream().anyMatch(inc -> "Encounter:patient".equals(inc.getValue()));

    List<IBaseResource> includedResources = new ArrayList<>();

    if (legacyPatientEntities.isEmpty()) {
      return includedResources;
    }

    if (includeObservations || includeEncounters) {
      List<String> patientUuids =
          legacyPatientEntities.getContent().stream().map(lp -> lp.getUuid().toString()).toList();

      if (includeObservations && !patientUuids.isEmpty()) {
        ObservationSearchQuery query =
            ObservationSearchQuery.builder().patientId(patientUuids).build();
        SearchResult observationSearchResult = observationService.search(query);
        includedResources.addAll(observationSearchResult.primaryResources());
      }

      if (includeEncounters && !patientUuids.isEmpty()) {
        EncounterSearchQuery query = EncounterSearchQuery.builder().patientId(patientUuids).build();
        SearchResult encounterSearchResult = encounterService.search(query);
        includedResources.addAll(encounterSearchResult.primaryResources());
      }
    }

    return includedResources;
  }
}

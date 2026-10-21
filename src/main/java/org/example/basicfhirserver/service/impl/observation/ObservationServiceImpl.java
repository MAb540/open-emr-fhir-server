package org.example.basicfhirserver.service.impl.observation;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.mapper.ObservationMapper;
import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.PatientService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service("ObservationServiceImpl")
public class ObservationServiceImpl implements ObservationService {

  private final List<ObservationProvider> observationProviders;
  private final ObservationMapper observationMapper;
  private final PatientService patientService;
  private final EncounterService encounterService;

  public ObservationServiceImpl(
      List<ObservationProvider> observationProviders,
      ObservationMapper observationMapper,
      // The patient service resolves observation rev-includes, so it is injected lazily to break
      // the Patient <-> Observation bean cycle.
      @Lazy PatientService patientService,
      EncounterService encounterService) {
    this.observationProviders = observationProviders;
    this.observationMapper = observationMapper;
    this.patientService = patientService;
    this.encounterService = encounterService;
  }

  @Override
  public Observation findById(UUID uuid) {
    Optional<CanonicalObservation> canonicalObservation =
        observationProviders.stream()
            .map(provider -> provider.fetchById(uuid))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst();

    if (canonicalObservation.isEmpty()) {
      throw new ResourceNotFoundException("Observation with given ID " + uuid + " not found.");
    }

    return observationMapper.toR4(canonicalObservation.get());
  }

  @Override
  public SearchResult search(ObservationSearchQuery observationSearchQuery) {
    List<CanonicalObservation> observations =
        observationProviders.stream()
            .filter(provider -> provider.supports(observationSearchQuery))
            .flatMap(provider -> provider.fetch(observationSearchQuery).stream())
            .toList();

    List<IBaseResource> primaryObservations =
        observations.stream().map(observationMapper::toR4).map(IBaseResource.class::cast).toList();

    List<IBaseResource> includedResources =
        includeResources(observations, observationSearchQuery.getIncludes());

    int currentOffset =
        observationSearchQuery.getOffset() != null ? observationSearchQuery.getOffset() : 0;

    // The observation providers page their own sources and expose no upstream total, so the
    // number of observations assembled for this page is the total the service can report.
    return new SearchResult(
        primaryObservations, includedResources, primaryObservations.size(), currentOffset);
  }

  private List<IBaseResource> includeResources(
      List<CanonicalObservation> observations, Set<Include> includes) {
    boolean includeSubjects =
        includes != null
            && includes.stream()
                .anyMatch(
                    inc ->
                        "Observation:subject".equals(inc.getValue())
                            || "Observation:patient".equals(inc.getValue()));

    boolean includeEncounters =
        includes != null
            && includes.stream().anyMatch(inc -> "Observation:encounter".equals(inc.getValue()));

    List<IBaseResource> includedResources = new ArrayList<>();

    if (observations.isEmpty()) {
      return includedResources;
    }

    if (includeSubjects) {
      List<String> patientUuids =
          observations.stream()
              .map(CanonicalObservation::getPatientUuid)
              .filter(uuid -> uuid != null && !uuid.isEmpty())
              .distinct()
              .toList();

      if (!patientUuids.isEmpty()) {
        PatientSearchQuery query = PatientSearchQuery.builder().patientId(patientUuids).build();
        SearchResult patientSearchResult = patientService.search(query);
        includedResources.addAll(patientSearchResult.primaryResources());
      }
    }

    if (includeEncounters) {
      List<String> encounterUuids =
          observations.stream()
              .map(CanonicalObservation::getEncounterUuid)
              .filter(uuid -> uuid != null && !uuid.isEmpty())
              .distinct()
              .toList();

      if (!encounterUuids.isEmpty()) {
        EncounterSearchQuery query =
            EncounterSearchQuery.builder().encounterId(encounterUuids).build();
        SearchResult encounterSearchResult = encounterService.search(query);
        includedResources.addAll(encounterSearchResult.primaryResources());
      }
    }

    return includedResources;
  }
}

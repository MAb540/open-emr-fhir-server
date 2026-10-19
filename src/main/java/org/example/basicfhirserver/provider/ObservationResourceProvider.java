package org.example.basicfhirserver.provider;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.annotation.*;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import java.util.*;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.mapper.ObservationMapper;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.provider.utils.BundleProvider;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchCriteria;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.query.translator.impl.ObservationSearchTranslator;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.PatientService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@SupportedProfiles(
    profile = ProfilesConstants.HL7_US_CORE_BODY_WEIGHT,
    supported = {
      ProfilesConstants.HL7_US_CORE_BODY_WEIGHT,
      ProfilesConstants.HL7_US_CORE_BODY_HEIGHT,
      ProfilesConstants.HL7_US_CORE_BODY_TEMPERATURE,
      ProfilesConstants.HL7_US_CORE_BMI,
      ProfilesConstants.HL7_US_CORE_BLOOD_PRESSURE
    })
public class ObservationResourceProvider implements IResourceProvider {

  private final ObservationService observationService;
  private final ObservationMapper observationMapper;
  private final ObservationSearchTranslator observationSearchTranslator;
  private final PatientService patientService;
  private final LegacyPatientMapper legacyPatientMapper;
  private final EncounterService encounterService;

  public ObservationResourceProvider(
      @Qualifier("ObservationServiceImpl") ObservationService observationService,
      ObservationMapper observationMapper,
      ObservationSearchTranslator observationSearchTranslator,
      PatientService patientService,
      LegacyPatientMapper legacyPatientMapper,
      EncounterService encounterService) {
    this.observationService = observationService;
    this.observationMapper = observationMapper;
    this.observationSearchTranslator = observationSearchTranslator;
    this.patientService = patientService;
    this.legacyPatientMapper = legacyPatientMapper;
    this.encounterService = encounterService;
  }

  @Override
  public Class<? extends IBaseResource> getResourceType() {
    return Observation.class;
  }

  @Read()
  public Observation getResourceById(@IdParam IdType theId) {
    UUID uuid = UUID.fromString(theId.getIdPart());
    Optional<CanonicalObservation> canonicalObservation = observationService.findById(uuid);

    if (canonicalObservation.isEmpty()) {
      throw new org.hl7.fhir.exceptions.FHIRException(
          "Observation not found with ID: " + theId.getIdPart());
    }
    return observationMapper.toR4(canonicalObservation.get());
  }

  @Search()
  public IBundleProvider searchObservations(
      @OptionalParam(name = Observation.SP_PATIENT) ReferenceParam patient,
      @OptionalParam(name = Observation.SP_CATEGORY) TokenOrListParam category,
      @OptionalParam(name = Observation.SP_CODE) TokenOrListParam codes,
      @OptionalParam(name = Observation.SP_DATE) DateParam date,
      @OptionalParam(name = Observation.SP_RES_LAST_UPDATED) DateParam lastUpdated,
      @IncludeParam(allow = {"Observation:subject", "Observation:patient", "Observation:encounter"})
          Set<Include> theIncludes,
      @Count Integer count,
      @Offset Integer offset) {

    ObservationSearchCriteria criteria =
        ObservationSearchCriteria.builder()
            .patient(patient)
            .category(category)
            .codes(codes)
            .date(date)
            .lastUpdated(lastUpdated)
            .count(count)
            .offset(offset)
            .build();

    var observationSearchQuery = observationSearchTranslator.translate(criteria);

    List<CanonicalObservation> observations = observationService.find(observationSearchQuery);

    List<IBaseResource> primaryObservations =
        observations.stream().<IBaseResource>map(observationMapper::toR4).toList();

    boolean includeSubjects =
        theIncludes != null
            && theIncludes.stream()
                .anyMatch(
                    inc ->
                        "Observation:subject".equals(inc.getValue())
                            || "Observation:patient".equals(inc.getValue()));

    boolean includeEncounters =
        theIncludes != null
            && theIncludes.stream().anyMatch(inc -> "Observation:encounter".equals(inc.getValue()));

    List<IBaseResource> includedResources = new ArrayList<>();

    if (!observations.isEmpty()) {

      if (includeSubjects) {
        List<String> patientUuids =
            observations.stream().map(CanonicalObservation::getPatientUuid).toList();
        PatientSearchQuery query = PatientSearchQuery.builder().patientId(patientUuids).build();

        Page<LegacyPatientEntity> legacyPatientEntities = patientService.find(query);
        legacyPatientEntities.getContent().stream()
            .<IBaseResource>map(legacyPatientMapper::toR4)
            .forEach(includedResources::add);
      }

      if (includeEncounters) {
        List<String> encounterUuids =
            observations.stream().map(CanonicalObservation::getEncounterUuid).toList();
        EncounterSearchQuery query =
            EncounterSearchQuery.builder().encounterId(encounterUuids).build();
        SearchResult encounterSearchResult = encounterService.search(query);
        includedResources.addAll(encounterSearchResult.primaryResources());
      }
    }

    int currentOffset = offset != null ? offset : 0;
    int currentPageSize = primaryObservations.size();

    return new BundleProvider(
        primaryObservations, includedResources, 100, currentOffset, currentPageSize);
  }
}

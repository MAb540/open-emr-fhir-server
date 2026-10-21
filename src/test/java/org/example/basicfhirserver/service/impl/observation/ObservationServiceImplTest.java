package org.example.basicfhirserver.service.impl.observation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.mapper.ObservationMapper;
import org.example.basicfhirserver.mapper.impl.ObservationMapperImpl;
import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.PatientService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ObservationServiceImplTest {

  @Mock private ObservationProvider observationProvider;
  @Mock private PatientService patientService;
  @Mock private EncounterService encounterService;

  // The mapper is a pure function over a DB record - there is no I/O to mock, so the real
  // implementation is used and the assertions target the returned FHIR resource.
  private final ObservationMapper observationMapper = new ObservationMapperImpl();

  private ObservationServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new ObservationServiceImpl(
            List.of(observationProvider), observationMapper, patientService, encounterService);
  }

  private static final UUID OBSERVATION_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID PATIENT_UUID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
  private static final UUID ENCOUNTER_UUID =
      UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
  private static final UUID PRACTITIONER_UUID =
      UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
  private static final LocalDateTime EFFECTIVE = LocalDateTime.of(2026, 1, 20, 11, 15);
  private static final String OBSERVATION_PROFILE =
      "http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab";

  private static CanonicalObservation canonicalObservation() {
    return CanonicalObservation.builder()
        .id(OBSERVATION_UUID.toString())
        .version("1")
        .status("final")
        .lastUpdated(EFFECTIVE)
        .patientUuid(PATIENT_UUID.toString())
        .encounterUuid(ENCOUNTER_UUID.toString())
        .practitionerUuid(PRACTITIONER_UUID.toString())
        .profile(OBSERVATION_PROFILE)
        .categories(List.of("laboratory"))
        .code("58410-2")
        .display("Complete blood count")
        .codeSystem("http://loinc.org")
        .effectiveDateTime(EFFECTIVE)
        .value(
            CanonicalObservation.ObservationValue.builder()
                .numericValue(12.5)
                .unit("mg")
                .unitSystem("http://unitsofmeasure.org")
                .build())
        .build();
  }

  @Test
  void findById_returnsMappedObservation() {
    when(observationProvider.fetchById(OBSERVATION_UUID))
        .thenReturn(Optional.of(canonicalObservation()));

    Observation result = service.findById(OBSERVATION_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(OBSERVATION_UUID.toString());
    assertThat(result.getStatus()).isEqualTo(Observation.ObservationStatus.FINAL);
    assertThat(result.getCategory().get(0).getCodingFirstRep().getCode()).isEqualTo("laboratory");
    assertThat(result.getCode().getCodingFirstRep().getCode()).isEqualTo("58410-2");
    assertThat(result.getCode().getCodingFirstRep().getDisplay()).isEqualTo("Complete blood count");
    assertThat(result.getSubject().getReference()).isEqualTo("Patient/" + PATIENT_UUID);
    assertThat(result.getEncounter().getReference()).isEqualTo("Encounter/" + ENCOUNTER_UUID);
    assertThat(result.getPerformer().get(0).getReference())
        .isEqualTo("Practitioner/" + PRACTITIONER_UUID);
    assertThat(result.getValueQuantity().getValue().doubleValue()).isEqualTo(12.5);
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(OBSERVATION_PROFILE);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecord() {
    when(observationProvider.fetchById(OBSERVATION_UUID)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(OBSERVATION_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(OBSERVATION_UUID.toString());
  }

  @Test
  void search_returnsPrimaryObservations() {
    ObservationSearchQuery query = ObservationSearchQuery.builder().build();
    when(observationProvider.supports(query)).thenReturn(true);
    when(observationProvider.fetch(query)).thenReturn(List.of(canonicalObservation()));

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(1);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.currentOffset()).isZero();

    Observation observation = (Observation) result.primaryResources().get(0);
    assertThat(observation.getId()).isEqualTo(OBSERVATION_UUID.toString());
    assertThat(observation.getSubject().getReference()).isEqualTo("Patient/" + PATIENT_UUID);
    assertThat(observation.getCode().getCodingFirstRep().getCode()).isEqualTo("58410-2");

    verify(patientService, never()).search(any(PatientSearchQuery.class));
    verify(encounterService, never()).search(any(EncounterSearchQuery.class));
  }

  @Test
  void search_includesPatientResourcesWhenRequested() {
    Set<Include> includes = Set.of(new Include("Observation:patient"));
    ObservationSearchQuery query = ObservationSearchQuery.builder().includes(includes).build();
    when(observationProvider.supports(query)).thenReturn(true);
    when(observationProvider.fetch(query)).thenReturn(List.of(canonicalObservation()));

    Patient patient = new Patient();
    patient.setId(PATIENT_UUID.toString());
    when(patientService.search(any(PatientSearchQuery.class)))
        .thenReturn(new SearchResult(List.of(patient), List.of(), 1, 0));

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(1);
    assertThat(result.includedResources()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1);

    Patient included = (Patient) result.includedResources().get(0);
    assertThat(included.getId()).isEqualTo(PATIENT_UUID.toString());

    ArgumentCaptor<PatientSearchQuery> queryCaptor =
        ArgumentCaptor.forClass(PatientSearchQuery.class);
    verify(patientService).search(queryCaptor.capture());
    assertThat(queryCaptor.getValue().getPatientId()).containsExactly(PATIENT_UUID.toString());
  }

  @Test
  void search_includesEncounterResourcesWhenRequested() {
    Set<Include> includes = Set.of(new Include("Observation:encounter"));
    ObservationSearchQuery query = ObservationSearchQuery.builder().includes(includes).build();
    when(observationProvider.supports(query)).thenReturn(true);
    when(observationProvider.fetch(query)).thenReturn(List.of(canonicalObservation()));

    Encounter encounter = new Encounter();
    encounter.setId(ENCOUNTER_UUID.toString());
    when(encounterService.search(any(EncounterSearchQuery.class)))
        .thenReturn(new SearchResult(List.of(encounter), List.of(), 1, 0));

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(1);
    assertThat(result.includedResources()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1);

    Encounter included = (Encounter) result.includedResources().get(0);
    assertThat(included.getId()).isEqualTo(ENCOUNTER_UUID.toString());

    ArgumentCaptor<EncounterSearchQuery> queryCaptor =
        ArgumentCaptor.forClass(EncounterSearchQuery.class);
    verify(encounterService).search(queryCaptor.capture());
    assertThat(queryCaptor.getValue().getEncounterId()).containsExactly(ENCOUNTER_UUID.toString());
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    ObservationSearchQuery query = ObservationSearchQuery.builder().build();
    when(observationProvider.supports(query)).thenReturn(true);
    when(observationProvider.fetch(query)).thenReturn(List.of());

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    ObservationSearchQuery query = ObservationSearchQuery.builder().count(10).offset(20).build();
    when(observationProvider.supports(query)).thenReturn(true);
    when(observationProvider.fetch(query)).thenReturn(List.of());

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(20);
  }
}

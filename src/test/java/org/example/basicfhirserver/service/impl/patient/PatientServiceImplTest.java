package org.example.basicfhirserver.service.impl.patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.mapper.ObservationMapper;
import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.repository.jpa.patient.PatientRepository;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class PatientServiceImplTest {

  @Mock private PatientRepository patientRepository;
  @Mock private LegacyPatientMapper legacyPatientMapper;
  @Mock private ObservationService observationService;
  @Mock private ObservationMapper observationMapper;
  @Mock private EncounterService encounterService;

  @InjectMocks private PatientServiceImpl service;

  private static final UUID PATIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void findById_returnsMappedPatient() {
    LegacyPatientEntity entity = mock(LegacyPatientEntity.class);
    Patient patient = new Patient();
    when(patientRepository.findOne(any(Specification.class))).thenReturn(Optional.of(entity));
    when(legacyPatientMapper.toR4(entity)).thenReturn(patient);

    Patient result = service.findById(PATIENT_UUID);

    assertThat(result).isSameAs(patient);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecord() {
    when(patientRepository.findOne(any(Specification.class))).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.findById(PATIENT_UUID))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(legacyPatientMapper, never()).toR4(any(LegacyPatientEntity.class));
  }

  @Test
  void search_returnsPrimaryPatientsWithoutIncludes() {
    PatientSearchQuery query = PatientSearchQuery.builder().build();

    LegacyPatientEntity entity = mock(LegacyPatientEntity.class);
    Page<LegacyPatientEntity> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(entity));
    when(page.getTotalElements()).thenReturn(1L);
    when(patientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

    Patient patient = new Patient();
    when(legacyPatientMapper.toR4(entity)).thenReturn(patient);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(patient);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.currentOffset()).isZero();
    verify(observationService, never()).find(any(ObservationSearchQuery.class));
    verify(encounterService, never()).search(any(EncounterSearchQuery.class));
  }

  @Test
  void search_includesObservationsWhenRevIncluded() {
    Set<Include> revIncludes = Set.of(new Include("Observation:patient"));
    PatientSearchQuery query = PatientSearchQuery.builder().revIncludes(revIncludes).build();

    LegacyPatientEntity entity = mock(LegacyPatientEntity.class);
    when(entity.getUuid()).thenReturn(PATIENT_UUID);
    Page<LegacyPatientEntity> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(entity));
    when(page.getTotalElements()).thenReturn(1L);
    when(patientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

    Patient patient = new Patient();
    when(legacyPatientMapper.toR4(entity)).thenReturn(patient);

    CanonicalObservation canonicalObservation = mock(CanonicalObservation.class);
    when(observationService.find(any(ObservationSearchQuery.class)))
        .thenReturn(List.of(canonicalObservation));
    Observation observation = new Observation();
    when(observationMapper.toR4(canonicalObservation)).thenReturn(observation);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(patient);
    assertThat(result.includedResources()).containsExactly(observation);
    verify(observationService).find(any(ObservationSearchQuery.class));
  }

  @Test
  void search_includesEncountersWhenRevIncluded() {
    Set<Include> revIncludes = Set.of(new Include("Encounter:patient"));
    PatientSearchQuery query = PatientSearchQuery.builder().revIncludes(revIncludes).build();

    LegacyPatientEntity entity = mock(LegacyPatientEntity.class);
    when(entity.getUuid()).thenReturn(PATIENT_UUID);
    Page<LegacyPatientEntity> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(entity));
    when(page.getTotalElements()).thenReturn(1L);
    when(patientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

    Patient patient = new Patient();
    when(legacyPatientMapper.toR4(entity)).thenReturn(patient);

    Encounter encounter = new Encounter();
    SearchResult encounterResult = new SearchResult(List.of(encounter), List.of(), 1, 0);
    when(encounterService.search(any(EncounterSearchQuery.class))).thenReturn(encounterResult);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(patient);
    assertThat(result.includedResources()).containsExactly(encounter);
    verify(encounterService).search(any(EncounterSearchQuery.class));
  }

  @Test
  void search_usesPagingFromQuery() {
    PatientSearchQuery query = PatientSearchQuery.builder().count(10).offset(20).build();

    LegacyPatientEntity entity = mock(LegacyPatientEntity.class);
    Page<LegacyPatientEntity> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(entity));
    when(page.getTotalElements()).thenReturn(1L);
    when(patientRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
    Patient patient = new Patient();
    when(legacyPatientMapper.toR4(entity)).thenReturn(patient);

    SearchResult result = service.search(query);

    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
    verify(patientRepository).findAll(any(Specification.class), pageableCaptor.capture());
    assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
    assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(10);
    assertThat(result.currentOffset()).isEqualTo(20);
  }
}

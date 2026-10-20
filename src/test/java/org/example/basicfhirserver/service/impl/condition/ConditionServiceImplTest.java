package org.example.basicfhirserver.service.impl.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.mapper.ConditionMapper;
import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionProblemListItemDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionRepository;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.PatientService;
import org.example.basicfhirserver.service.assembler.condition.ConditionAssembler;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class ConditionServiceImplTest {

  @Mock private ConditionRepository conditionRepository;
  @Mock private ConditionAssembler conditionAssembler;
  @Mock private ConditionMapper conditionMapper;
  @Mock private PatientService patientService;
  @Mock private EncounterService encounterService;

  @InjectMocks private ConditionServiceImpl service;

  private static final UUID CONDITION_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final String PATIENT_UUID = "223e4567-e89b-12d3-a456-426614174001";
  private static final String ENCOUNTER_UUID = "323e4567-e89b-12d3-a456-426614174002";

  @Test
  void findById_returnsMappedConditionFromProblemListItem() {
    ConditionProblemListItemDBRecord problemRecord = mock(ConditionProblemListItemDBRecord.class);
    ConditionCanonical canonical = ConditionCanonical.builder().patientUuid(PATIENT_UUID).build();
    Condition condition = new Condition();

    when(conditionRepository.findConditionProblemListItemById(CONDITION_UUID))
        .thenReturn(List.of(problemRecord));
    when(conditionRepository.findConditionEncounterDiagnosisById(CONDITION_UUID))
        .thenReturn(List.of());
    when(conditionRepository.findConditionHealthConcernDiagnosisById(CONDITION_UUID))
        .thenReturn(List.of());
    when(conditionAssembler.toCanonical(problemRecord)).thenReturn(canonical);
    when(conditionMapper.toR4(canonical)).thenReturn(condition);

    Condition result = service.findById(CONDITION_UUID);

    assertThat(result).isSameAs(condition);
    verify(conditionMapper).toR4(canonical);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecords() {
    when(conditionRepository.findConditionProblemListItemById(CONDITION_UUID))
        .thenReturn(List.of());
    when(conditionRepository.findConditionEncounterDiagnosisById(CONDITION_UUID))
        .thenReturn(List.of());
    when(conditionRepository.findConditionHealthConcernDiagnosisById(CONDITION_UUID))
        .thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(CONDITION_UUID))
        .isInstanceOf(ResourceNotFoundException.class);

    verifyNoInteractions(conditionAssembler, conditionMapper);
  }

  @Test
  void search_returnsPrimaryConditionsWithoutIncludes() {
    ConditionSearchQuery query =
        ConditionSearchQuery.builder().category("problem-list-item").build();

    ConditionProblemListItemDBRecord record = mock(ConditionProblemListItemDBRecord.class);
    Page<ConditionProblemListItemDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(record));
    when(page.getTotalElements()).thenReturn(1L);
    when(conditionRepository.findConditionProblemListItem(any(ConditionSearchQuery.class)))
        .thenReturn(page);

    ConditionCanonical canonical = ConditionCanonical.builder().patientUuid(PATIENT_UUID).build();
    when(conditionAssembler.toCanonical(record)).thenReturn(canonical);
    Condition condition = new Condition();
    when(conditionMapper.toR4(canonical)).thenReturn(condition);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(condition);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(1);
    verify(patientService, never()).search(any(PatientSearchQuery.class));
    verify(encounterService, never()).search(any(EncounterSearchQuery.class));
  }

  @Test
  void search_includesPatientsWhenSubjectIncluded() {
    Set<Include> includes = Set.of(new Include("Condition:subject"));
    ConditionSearchQuery query =
        ConditionSearchQuery.builder().category("problem-list-item").includes(includes).build();

    ConditionProblemListItemDBRecord record = mock(ConditionProblemListItemDBRecord.class);
    Page<ConditionProblemListItemDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(record));
    when(page.getTotalElements()).thenReturn(1L);
    when(conditionRepository.findConditionProblemListItem(any(ConditionSearchQuery.class)))
        .thenReturn(page);

    ConditionCanonical canonical = ConditionCanonical.builder().patientUuid(PATIENT_UUID).build();
    when(conditionAssembler.toCanonical(record)).thenReturn(canonical);
    Condition condition = new Condition();
    when(conditionMapper.toR4(canonical)).thenReturn(condition);

    Patient patient = new Patient();
    SearchResult patientSearchResult = new SearchResult(List.of(patient), List.of(), 1, 0);
    when(patientService.search(any(PatientSearchQuery.class))).thenReturn(patientSearchResult);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(condition);
    assertThat(result.includedResources()).containsExactly(patient);
    assertThat(result.totalElements()).isEqualTo(1);

    ArgumentCaptor<PatientSearchQuery> queryCaptor =
        ArgumentCaptor.forClass(PatientSearchQuery.class);
    verify(patientService).search(queryCaptor.capture());
    assertThat(queryCaptor.getValue().getPatientId()).containsExactly(PATIENT_UUID);
  }

  @Test
  void search_includesEncountersWhenEncounterIncluded() {
    Set<Include> includes = Set.of(new Include("Condition:encounter"));
    ConditionSearchQuery query =
        ConditionSearchQuery.builder().category("problem-list-item").includes(includes).build();

    ConditionProblemListItemDBRecord record = mock(ConditionProblemListItemDBRecord.class);
    Page<ConditionProblemListItemDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(record));
    when(page.getTotalElements()).thenReturn(1L);
    when(conditionRepository.findConditionProblemListItem(any(ConditionSearchQuery.class)))
        .thenReturn(page);

    ConditionCanonical canonical =
        ConditionCanonical.builder().encounterUuid(ENCOUNTER_UUID).build();
    when(conditionAssembler.toCanonical(record)).thenReturn(canonical);
    Condition condition = new Condition();
    when(conditionMapper.toR4(canonical)).thenReturn(condition);

    Encounter encounter = new Encounter();
    SearchResult encounterResult = new SearchResult(List.of(encounter), List.of(), 1, 0);
    when(encounterService.search(any(EncounterSearchQuery.class))).thenReturn(encounterResult);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(condition);
    assertThat(result.includedResources()).containsExactly(encounter);
    assertThat(result.totalElements()).isEqualTo(1);

    ArgumentCaptor<EncounterSearchQuery> queryCaptor =
        ArgumentCaptor.forClass(EncounterSearchQuery.class);
    verify(encounterService).search(queryCaptor.capture());
    assertThat(queryCaptor.getValue().getEncounterId()).containsExactly(ENCOUNTER_UUID);
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    ConditionSearchQuery query =
        ConditionSearchQuery.builder().category("problem-list-item").build();

    Page<ConditionProblemListItemDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
    when(conditionRepository.findConditionProblemListItem(any(ConditionSearchQuery.class)))
        .thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
    verify(patientService, never()).search(any(PatientSearchQuery.class));
    verify(encounterService, never()).search(any(EncounterSearchQuery.class));
  }
}

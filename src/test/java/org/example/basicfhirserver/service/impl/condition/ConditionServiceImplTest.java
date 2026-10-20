package org.example.basicfhirserver.service.impl.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.mapper.ConditionMapper;
import org.example.basicfhirserver.mapper.impl.ConditionMapperImpl;
import org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionEncounterDiagnosisDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionHealthConcernDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionProblemListItemDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionRepository;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.PatientService;
import org.example.basicfhirserver.service.assembler.condition.ConditionAssembler;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class ConditionServiceImplTest {

  @Mock private ConditionRepository conditionRepository;
  @Mock private PatientService patientService;
  @Mock private EncounterService encounterService;

  // The assembler and the mapper are pure functions over a DB record - there is no I/O to
  // mock, so the real implementations are used and the assertions target the returned resource.
  private final ConditionAssembler conditionAssembler = new ConditionAssembler();
  private final ConditionMapper conditionMapper = new ConditionMapperImpl();

  private ConditionServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new ConditionServiceImpl(
            conditionRepository,
            conditionAssembler,
            conditionMapper,
            patientService,
            encounterService);
  }

  private static final UUID CONDITION_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID PATIENT_UUID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
  private static final UUID ENCOUNTER_UUID =
      UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
  private static final UUID CREATOR_UUID = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
  private static final LocalDateTime CONDITION_DATE = LocalDateTime.of(2026, 1, 10, 9, 0, 30);

  private static ConditionProblemListItemDBRecord problemListRecord() {
    return ConditionProblemListItemDBRecord.builder()
        .id(1L)
        .uuid(CONDITION_UUID)
        .pid(42L)
        .puuid(PATIENT_UUID)
        .conditionDate(CONDITION_DATE)
        .begdate(CONDITION_DATE)
        .occurrence(1)
        .outcome(0)
        .verification("confirmed")
        .title("Asthma")
        .lastUpdatedTime(CONDITION_DATE)
        .build();
  }

  private static ConditionEncounterDiagnosisDBRecord encounterDiagnosisRecord() {
    return ConditionEncounterDiagnosisDBRecord.builder()
        .id(2L)
        .uuid(CONDITION_UUID)
        .listsUuid(CONDITION_UUID.toString())
        .pid(42L)
        .puuid(PATIENT_UUID)
        .date(CONDITION_DATE)
        .begdate(CONDITION_DATE)
        .resolved(1)
        .verification("confirmed")
        .title("Acute bronchitis")
        .encounterUuid(ENCOUNTER_UUID)
        .creatorUuid(CREATOR_UUID)
        .creatorNpi("1234567890")
        .updatorUuid(CREATOR_UUID)
        .lastUpdatedTime(CONDITION_DATE)
        .build();
  }

  private static ConditionHealthConcernDBRecord healthConcernRecord() {
    return ConditionHealthConcernDBRecord.builder()
        .id(3L)
        .uuid(CONDITION_UUID)
        .pid(42L)
        .puuid(PATIENT_UUID)
        .conditionDate(CONDITION_DATE)
        .begdate(CONDITION_DATE)
        .occurrence(2)
        .outcome(0)
        .verification("unconfirmed")
        .title("Smoking habit")
        .healthConcernSubtype("exam")
        .healthConcernSubtypeTitle("Physical exam")
        .lastUpdatedTime(CONDITION_DATE)
        .build();
  }

  @Test
  void findById_returnsMappedConditionFromProblemListItem() {
    when(conditionRepository.findConditionProblemListItemById(CONDITION_UUID))
        .thenReturn(List.of(problemListRecord()));
    when(conditionRepository.findConditionEncounterDiagnosisById(CONDITION_UUID))
        .thenReturn(List.of());
    when(conditionRepository.findConditionHealthConcernDiagnosisById(CONDITION_UUID))
        .thenReturn(List.of());

    Condition result = service.findById(CONDITION_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(CONDITION_UUID.toString());
    assertThat(result.getCategory().get(0).getCodingFirstRep().getCode())
        .isEqualTo(ConditionAssembler.CATEGORY_PROBLEM_LIST);
    assertThat(result.getCategory().get(0).getCodingFirstRep().getDisplay())
        .isEqualTo("Problem List Item");
    assertThat(result.getClinicalStatus().getCodingFirstRep().getCode()).isEqualTo("resolved");
    assertThat(result.getVerificationStatus().getCodingFirstRep().getCode()).isEqualTo("confirmed");
    assertThat(result.getSubject().getReference()).isEqualTo("Patient/" + PATIENT_UUID);
    assertThat(result.getRecordedDate())
        .isEqualTo(Date.from(CONDITION_DATE.atZone(ZoneId.systemDefault()).toInstant()));
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(
            ProfilesConstants.HL7_US_CORE_CONDITION,
            ProfilesConstants.HL7_US_CORE_CONDITION_PROBLEMS_HEALTH_CONCERNS);
  }

  @Test
  void findById_returnsMappedConditionFromEncounterDiagnosis() {
    when(conditionRepository.findConditionProblemListItemById(CONDITION_UUID))
        .thenReturn(List.of());
    when(conditionRepository.findConditionEncounterDiagnosisById(CONDITION_UUID))
        .thenReturn(List.of(encounterDiagnosisRecord()));
    when(conditionRepository.findConditionHealthConcernDiagnosisById(CONDITION_UUID))
        .thenReturn(List.of());

    Condition result = service.findById(CONDITION_UUID);

    assertThat(result.getId()).isEqualTo(CONDITION_UUID.toString());
    assertThat(result.getCategory().get(0).getCodingFirstRep().getCode())
        .isEqualTo(ConditionAssembler.CATEGORY_ENCOUNTER_DIAGNOSIS);
    assertThat(result.getEncounter().getReference()).isEqualTo("Encounter/" + ENCOUNTER_UUID);
    assertThat(result.getRecorder().getReference()).isEqualTo("Practitioner/" + CREATOR_UUID);
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(ProfilesConstants.HL7_US_CORE_CONDITION_ENCOUNTER_DIAGNOSIS);
  }

  @Test
  void findById_returnsMappedConditionFromHealthConcern() {
    when(conditionRepository.findConditionProblemListItemById(CONDITION_UUID))
        .thenReturn(List.of());
    when(conditionRepository.findConditionEncounterDiagnosisById(CONDITION_UUID))
        .thenReturn(List.of());
    when(conditionRepository.findConditionHealthConcernDiagnosisById(CONDITION_UUID))
        .thenReturn(List.of(healthConcernRecord()));

    Condition result = service.findById(CONDITION_UUID);

    assertThat(result.getId()).isEqualTo(CONDITION_UUID.toString());
    assertThat(result.getCategory().get(0).getCodingFirstRep().getCode())
        .isEqualTo(ConditionAssembler.CATEGORY_HEALTH_CONCERNS);
    assertThat(result.getCategory().get(0).getCodingFirstRep().getDisplay())
        .isEqualTo("Health Concern");
    assertThat(result.getCategory().get(0).getCoding().get(1).getCode()).isEqualTo("exam");
    assertThat(result.getCategory().get(0).getCoding().get(1).getDisplay())
        .isEqualTo("Physical exam");
    assertThat(result.getCategory().get(0).getCoding().get(1).getSystem())
        .isEqualTo(FhirCodeSystemConstants.HL7_CATEGORY_OBSERVATION);
    assertThat(result.getClinicalStatus().getCodingFirstRep().getCode()).isEqualTo("recurrence");
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
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(CONDITION_UUID.toString());
  }

  @Test
  void search_returnsPrimaryConditionsWithoutIncludes() {
    ConditionSearchQuery query =
        ConditionSearchQuery.builder().category(ConditionAssembler.CATEGORY_PROBLEM_LIST).build();

    Page<ConditionProblemListItemDBRecord> page = new PageImpl<>(List.of(problemListRecord()));

    when(conditionRepository.findConditionProblemListItem(any(ConditionSearchQuery.class)))
        .thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(1);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(1);

    Condition condition = (Condition) result.primaryResources().get(0);
    assertThat(condition.getId()).isEqualTo(CONDITION_UUID.toString());
    assertThat(condition.getSubject().getReference()).isEqualTo("Patient/" + PATIENT_UUID);

    verify(patientService, never()).search(any(PatientSearchQuery.class));
    verify(encounterService, never()).search(any(EncounterSearchQuery.class));
  }

  @Test
  void search_includesPatientsWhenSubjectIncluded() {
    Set<Include> includes = Set.of(new Include("Condition:subject"));
    ConditionSearchQuery query =
        ConditionSearchQuery.builder()
            .category(ConditionAssembler.CATEGORY_PROBLEM_LIST)
            .includes(includes)
            .build();

    Page<ConditionProblemListItemDBRecord> page = new PageImpl<>(List.of(problemListRecord()));

    when(conditionRepository.findConditionProblemListItem(any(ConditionSearchQuery.class)))
        .thenReturn(page);

    Patient patient = new Patient();
    patient.setId(PATIENT_UUID.toString());
    patient.addName(new HumanName().setFamily("Smith").addGiven("Jane"));
    when(patientService.search(any(PatientSearchQuery.class)))
        .thenReturn(new SearchResult(List.of(patient), List.of(), 1, 0));

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(1);
    assertThat(result.includedResources()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1);

    Patient included = (Patient) result.includedResources().get(0);
    assertThat(included.getId()).isEqualTo(PATIENT_UUID.toString());
    assertThat(included.getName().get(0).getFamily()).isEqualTo("Smith");

    ArgumentCaptor<PatientSearchQuery> queryCaptor =
        ArgumentCaptor.forClass(PatientSearchQuery.class);
    verify(patientService).search(queryCaptor.capture());
    assertThat(queryCaptor.getValue().getPatientId()).containsExactly(PATIENT_UUID.toString());
  }

  @Test
  void search_includesEncountersWhenEncounterIncluded() {
    Set<Include> includes = Set.of(new Include("Condition:encounter"));
    ConditionSearchQuery query =
        ConditionSearchQuery.builder()
            .category(ConditionAssembler.CATEGORY_ENCOUNTER_DIAGNOSIS)
            .includes(includes)
            .build();

    Page<ConditionEncounterDiagnosisDBRecord> page =
        new PageImpl<>(List.of(encounterDiagnosisRecord()));

    when(conditionRepository.findConditionEncounterDiagnosis(any(ConditionSearchQuery.class)))
        .thenReturn(page);

    Encounter encounter = new Encounter();
    encounter.setId(ENCOUNTER_UUID.toString());
    encounter.setStatus(Encounter.EncounterStatus.FINISHED);
    when(encounterService.search(any(EncounterSearchQuery.class)))
        .thenReturn(new SearchResult(List.of(encounter), List.of(), 1, 0));

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(1);
    assertThat(result.includedResources()).hasSize(1);
    assertThat(result.totalElements()).isEqualTo(1);

    Encounter included = (Encounter) result.includedResources().get(0);
    assertThat(included.getId()).isEqualTo(ENCOUNTER_UUID.toString());
    assertThat(included.getStatus()).isEqualTo(Encounter.EncounterStatus.FINISHED);

    ArgumentCaptor<EncounterSearchQuery> queryCaptor =
        ArgumentCaptor.forClass(EncounterSearchQuery.class);
    verify(encounterService).search(queryCaptor.capture());
    assertThat(queryCaptor.getValue().getEncounterId()).containsExactly(ENCOUNTER_UUID.toString());
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    ConditionSearchQuery query =
        ConditionSearchQuery.builder().category(ConditionAssembler.CATEGORY_PROBLEM_LIST).build();

    Page<ConditionProblemListItemDBRecord> page = new PageImpl<>(List.of());

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

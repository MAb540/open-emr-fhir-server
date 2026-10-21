package org.example.basicfhirserver.service.impl.allergyIntolerance;

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
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.mapper.AllergyIntoleranceMapper;
import org.example.basicfhirserver.mapper.impl.AllergyIntoleranceMapperImpl;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyRepository;
import org.example.basicfhirserver.service.PatientService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.CanonicalType;
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
class AllergyIntoleranceServiceImplTest {

  @Mock private AllergyRepository allergyRepository;
  @Mock private PatientService patientService;

  // The mapper is a pure function over a DB record - there is no I/O to mock, so the real
  // implementation is used and the assertions target the returned FHIR resource.
  private final AllergyIntoleranceMapper allergyIntoleranceMapper =
      new AllergyIntoleranceMapperImpl();

  private AllergyIntoleranceServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new AllergyIntoleranceServiceImpl(
            allergyRepository, allergyIntoleranceMapper, patientService);
  }

  private static final UUID ALLERGY_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID PATIENT_UUID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
  private static final UUID PRACTITIONER_UUID =
      UUID.fromString("323e4567-e89b-12d3-a456-426614174002");

  private static AllergyDBRecord record() {
    return AllergyDBRecord.builder()
        .id(1L)
        .uuid(ALLERGY_UUID)
        .title("Penicillin allergy")
        .outcome(1)
        .severityAl("mild")
        .pid(42L)
        .puuid(PATIENT_UUID)
        .patientUuid(PATIENT_UUID)
        .practitionerUuid(PRACTITIONER_UUID)
        .verification("confirmed")
        .verificationTitle("Confirmed")
        .modifydate(LocalDateTime.of(2026, 1, 16, 8, 0))
        .build();
  }

  @Test
  void findById_returnsMappedAllergy() {
    when(allergyRepository.findById(ALLERGY_UUID)).thenReturn(List.of(record()));

    AllergyIntolerance result = service.findById(ALLERGY_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(ALLERGY_UUID.toString());
    assertThat(result.getClinicalStatus().getCodingFirstRep().getCode()).isEqualTo("active");
    assertThat(result.getCategory().get(0).getValue())
        .isEqualTo(AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION);
    assertThat(result.getCriticality())
        .isEqualTo(AllergyIntolerance.AllergyIntoleranceCriticality.LOW);
    assertThat(result.getPatient().getReference()).isEqualTo("Patient/" + PATIENT_UUID);
    assertThat(result.getRecorder().getReference()).isEqualTo("Practitioner/" + PRACTITIONER_UUID);
    assertThat(result.getVerificationStatus().getCodingFirstRep().getCode()).isEqualTo("confirmed");
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(ProfilesConstants.HL7_US_CORE_ALLERGYINTOLERANCE);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecord() {
    when(allergyRepository.findById(ALLERGY_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(ALLERGY_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(ALLERGY_UUID.toString());
  }

  @Test
  void search_returnsAllergiesWithoutIncludes() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();

    Page<AllergyDBRecord> page = new PageImpl<>(List.of(record()));

    when(allergyRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(1);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(1);

    AllergyIntolerance allergy = (AllergyIntolerance) result.primaryResources().get(0);
    assertThat(allergy.getId()).isEqualTo(ALLERGY_UUID.toString());
    assertThat(allergy.getPatient().getReference()).isEqualTo("Patient/" + PATIENT_UUID);

    verify(patientService, never()).search(any(PatientSearchQuery.class));
  }

  @Test
  void search_includesPatientResourcesWhenRequested() {
    Set<Include> includes = Set.of(new Include("AllergyIntolerance:patient"));
    AllergyIntoleranceSearchQuery query =
        AllergyIntoleranceSearchQuery.builder().includes(includes).build();

    Page<AllergyDBRecord> page = new PageImpl<>(List.of(record()));

    when(allergyRepository.find(query)).thenReturn(page);

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
  void search_returnsEmptyResultWhenNoRecords() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();

    Page<AllergyDBRecord> page = new PageImpl<>(List.of());

    when(allergyRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    verify(patientService, never()).search(any(PatientSearchQuery.class));
  }
}

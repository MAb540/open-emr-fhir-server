package org.example.basicfhirserver.service.impl.allergyIntolerance;

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
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.AllergyIntoleranceMapper;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyRepository;
import org.example.basicfhirserver.service.PatientService;
import org.hl7.fhir.r4.model.AllergyIntolerance;
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
class AllergyIntoleranceServiceImplTest {

  @Mock private AllergyRepository allergyRepository;
  @Mock private AllergyIntoleranceMapper allergyIntoleranceMapper;
  @Mock private PatientService patientService;
  @Mock private LegacyPatientMapper legacyPatientMapper;

  @InjectMocks private AllergyIntoleranceServiceImpl service;

  private static final UUID ALLERGY_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID PATIENT_UUID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");

  @Test
  void findById_returnsMappedAllergy() {
    AllergyDBRecord record = mock(AllergyDBRecord.class);
    AllergyIntolerance allergy = new AllergyIntolerance();
    when(allergyRepository.findById(ALLERGY_UUID)).thenReturn(List.of(record));
    when(allergyIntoleranceMapper.toR4(record)).thenReturn(allergy);

    AllergyIntolerance result = service.findById(ALLERGY_UUID);

    assertThat(result).isSameAs(allergy);
    verify(allergyIntoleranceMapper).toR4(record);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecord() {
    when(allergyRepository.findById(ALLERGY_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(ALLERGY_UUID))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(allergyIntoleranceMapper, never()).toR4(any(AllergyDBRecord.class));
  }

  @Test
  void find_returnsPrimaryAllergiesWithoutIncludes() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();

    AllergyDBRecord record = mock(AllergyDBRecord.class);
    Page<AllergyDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(record));
    when(page.getTotalElements()).thenReturn(1L);
    when(allergyRepository.find(query)).thenReturn(page);

    AllergyIntolerance allergy = new AllergyIntolerance();
    when(allergyIntoleranceMapper.toR4(record)).thenReturn(allergy);

    AllergySearchResult result = service.find(query);

    assertThat(result.primaryResources()).containsExactly(allergy);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(1L);
    verify(patientService, never()).find(any(PatientSearchQuery.class));
  }

  @Test
  void find_includesPatientResourcesWhenRequested() {
    Set<Include> includes = Set.of(new Include("AllergyIntolerance:patient"));
    AllergyIntoleranceSearchQuery query =
        AllergyIntoleranceSearchQuery.builder().includes(includes).build();

    AllergyDBRecord record = mock(AllergyDBRecord.class);
    when(record.getPatientUuid()).thenReturn(PATIENT_UUID);
    Page<AllergyDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(record));
    when(page.getTotalElements()).thenReturn(1L);
    when(allergyRepository.find(query)).thenReturn(page);

    AllergyIntolerance allergy = new AllergyIntolerance();
    when(allergyIntoleranceMapper.toR4(record)).thenReturn(allergy);

    LegacyPatientEntity patientEntity = mock(LegacyPatientEntity.class);
    Page<LegacyPatientEntity> patientPage = mock(Page.class);
    when(patientPage.getContent()).thenReturn(List.of(patientEntity));
    when(patientService.find(any(PatientSearchQuery.class))).thenReturn(patientPage);
    Patient patient = new Patient();
    when(legacyPatientMapper.toR4(patientEntity)).thenReturn(patient);

    AllergySearchResult result = service.find(query);

    assertThat(result.primaryResources()).containsExactly(allergy);
    assertThat(result.includedResources()).containsExactly(patient);
    assertThat(result.totalElements()).isEqualTo(1L);

    ArgumentCaptor<PatientSearchQuery> queryCaptor =
        ArgumentCaptor.forClass(PatientSearchQuery.class);
    verify(patientService).find(queryCaptor.capture());
    assertThat(queryCaptor.getValue().getPatientId()).containsExactly(PATIENT_UUID.toString());
  }

  @Test
  void find_returnsEmptyResultWhenNoRecords() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();

    Page<AllergyDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
    when(allergyRepository.find(query)).thenReturn(page);

    AllergySearchResult result = service.find(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    verify(patientService, never()).find(any(PatientSearchQuery.class));
  }
}

package org.example.basicfhirserver.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.AllergyIntoleranceMapper;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchCriteria;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.query.translator.impl.AllergyIntoleranceSearchTranslator;
import org.example.basicfhirserver.repository.jdbc.allergy.AllergyDBRecord;
import org.example.basicfhirserver.service.AllergyIntoleranceService;
import org.example.basicfhirserver.service.PatientService;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.IdType;
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
class AllergyIntoleranceProviderTest {

  @Mock private AllergyIntoleranceService allergyIntoleranceService;
  @Mock private AllergyIntoleranceMapper allergyIntoleranceMapper;
  @Mock private AllergyIntoleranceSearchTranslator allergyIntoleranceSearchTranslator;
  @Mock private PatientService patientService;
  @Mock private LegacyPatientMapper legacyPatientMapper;

  @InjectMocks private AllergyIntoleranceProvider provider;

  private static final UUID ALLERGY_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void getResourceById_returnsMappedAllergyIntolerance() {
    IdType id = new IdType(ALLERGY_UUID.toString());
    AllergyDBRecord record = mock(AllergyDBRecord.class);
    AllergyIntolerance allergy = new AllergyIntolerance();
    when(allergyIntoleranceService.findById(ALLERGY_UUID)).thenReturn(record);
    when(allergyIntoleranceMapper.toR4(record)).thenReturn(allergy);

    AllergyIntolerance result = provider.getResourceById(id);

    assertThat(result).isSameAs(allergy);
    verify(allergyIntoleranceService).findById(ALLERGY_UUID);
    verify(allergyIntoleranceMapper).toR4(record);
  }

  @Test
  void getResourceById_propagatesNotFound() {
    IdType id = new IdType(ALLERGY_UUID.toString());
    when(allergyIntoleranceService.findById(ALLERGY_UUID))
        .thenThrow(new ResourceNotFoundException("not found"));

    assertThatThrownBy(() -> provider.getResourceById(id))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(allergyIntoleranceMapper, never()).toR4(any(AllergyDBRecord.class));
  }

  @Test
  void searchAllergyIntolerance_returnsMappedResourcesWithoutIncludes() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();
    when(allergyIntoleranceSearchTranslator.translate(any(AllergyIntoleranceSearchCriteria.class)))
        .thenReturn(query);

    AllergyDBRecord record = mock(AllergyDBRecord.class);
    Page<AllergyDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(record));
    when(page.getTotalElements()).thenReturn(1L);
    when(page.isEmpty()).thenReturn(false);
    when(allergyIntoleranceService.find(query)).thenReturn(page);

    AllergyIntolerance allergy = new AllergyIntolerance();
    when(allergyIntoleranceMapper.toR4(record)).thenReturn(allergy);

    IBundleProvider bundle =
        provider.searchAllergyIntolerance(null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(1);
    assertThat(bundle.getResources(0, 10)).containsExactly(allergy);
    assertThat(bundle.getCurrentPageOffset()).isZero();
    assertThat(bundle.getCurrentPageSize()).isEqualTo(1);
    verify(patientService, never()).find(any(PatientSearchQuery.class));
  }

  @Test
  void searchAllergyIntolerance_includesPatientResources() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();
    when(allergyIntoleranceSearchTranslator.translate(any(AllergyIntoleranceSearchCriteria.class)))
        .thenReturn(query);

    AllergyDBRecord record = mock(AllergyDBRecord.class);
    when(record.getPatientUuid()).thenReturn(ALLERGY_UUID);
    Page<AllergyDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(record));
    when(page.getTotalElements()).thenReturn(1L);
    when(page.isEmpty()).thenReturn(false);
    when(allergyIntoleranceService.find(query)).thenReturn(page);

    AllergyIntolerance allergy = new AllergyIntolerance();
    when(allergyIntoleranceMapper.toR4(record)).thenReturn(allergy);

    LegacyPatientEntity patientEntity = mock(LegacyPatientEntity.class);
    Page<LegacyPatientEntity> patientPage = mock(Page.class);
    when(patientPage.getContent()).thenReturn(List.of(patientEntity));
    when(patientService.find(any(PatientSearchQuery.class))).thenReturn(patientPage);
    Patient patient = new Patient();
    when(legacyPatientMapper.toR4(patientEntity)).thenReturn(patient);

    Set<Include> includes = Set.of(new Include("AllergyIntolerance:patient"));

    IBundleProvider bundle =
        provider.searchAllergyIntolerance(null, null, null, includes, null, null);

    assertThat(bundle.getResources(0, 10)).containsExactly(allergy, patient);
    verify(patientService).find(any(PatientSearchQuery.class));
  }

  @Test
  void searchAllergyIntolerance_returnsEmptyBundleWhenNoResults() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();
    when(allergyIntoleranceSearchTranslator.translate(any(AllergyIntoleranceSearchCriteria.class)))
        .thenReturn(query);

    Page<AllergyDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
    when(page.isEmpty()).thenReturn(true);
    when(allergyIntoleranceService.find(query)).thenReturn(page);

    IBundleProvider bundle =
        provider.searchAllergyIntolerance(null, null, null, null, null, null);

    assertThat(bundle.size()).isZero();
    assertThat(bundle.getResources(0, 10)).isEmpty();
  }

  @Test
  void searchAllergyIntolerance_buildsCriteriaFromSearchParams() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();
    when(allergyIntoleranceSearchTranslator.translate(any(AllergyIntoleranceSearchCriteria.class)))
        .thenReturn(query);

    Page<AllergyDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
    when(page.isEmpty()).thenReturn(true);
    when(allergyIntoleranceService.find(query)).thenReturn(page);

    TokenParam id = new TokenParam("123");
    ReferenceParam patient = new ReferenceParam("Patient/abc");
    provider.searchAllergyIntolerance(id, patient, null, null, 10, 5);

    ArgumentCaptor<AllergyIntoleranceSearchCriteria> captor =
        ArgumentCaptor.forClass(AllergyIntoleranceSearchCriteria.class);
    verify(allergyIntoleranceSearchTranslator).translate(captor.capture());
    assertThat(captor.getValue().getId()).isSameAs(id);
    assertThat(captor.getValue().getPatient()).isSameAs(patient);
    assertThat(captor.getValue().getCount()).isEqualTo(10);
    assertThat(captor.getValue().getOffset()).isEqualTo(5);
  }
}

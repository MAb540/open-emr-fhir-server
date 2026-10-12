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
import org.example.basicfhirserver.mapper.ConditionMapper;
import org.example.basicfhirserver.mapper.EncounterMapper;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchCriteria;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.query.translator.impl.ConditionTranslator;
import org.example.basicfhirserver.service.ConditionService;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.PatientService;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.Encounter;
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
class ConditionResourceProviderTest {

  @Mock private ConditionService conditionService;
  @Mock private ConditionMapper conditionMapper;
  @Mock private ConditionTranslator conditionTranslator;
  @Mock private PatientService patientService;
  @Mock private LegacyPatientMapper legacyPatientMapper;
  @Mock private EncounterService encounterService;
  @Mock private EncounterMapper encounterMapper;

  @InjectMocks private ConditionResourceProvider provider;

  private static final UUID CONDITION_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void getResourceById_returnsMappedCondition() {
    IdType id = new IdType(CONDITION_UUID.toString());
    ConditionCanonical canonical = mock(ConditionCanonical.class);
    Condition condition = new Condition();
    when(conditionService.findById(CONDITION_UUID)).thenReturn(canonical);
    when(conditionMapper.toR4(canonical)).thenReturn(condition);

    Condition result = provider.getResourceById(id);

    assertThat(result).isSameAs(condition);
    verify(conditionService).findById(CONDITION_UUID);
    verify(conditionMapper).toR4(canonical);
  }

  @Test
  void getResourceById_propagatesNotFound() {
    IdType id = new IdType(CONDITION_UUID.toString());
    when(conditionService.findById(CONDITION_UUID))
        .thenThrow(new ResourceNotFoundException("not found"));

    assertThatThrownBy(() -> provider.getResourceById(id))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(conditionMapper, never()).toR4(any(ConditionCanonical.class));
  }

  @Test
  void searchConditions_returnsMappedResourcesWithoutIncludes() {
    ConditionSearchQuery query = ConditionSearchQuery.builder().build();
    when(conditionTranslator.translate(any(ConditionSearchCriteria.class))).thenReturn(query);

    ConditionCanonical canonical = mock(ConditionCanonical.class);
    Page<ConditionCanonical> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(canonical));
    when(page.getTotalElements()).thenReturn(1L);
    when(page.isEmpty()).thenReturn(false);
    when(conditionService.find(query)).thenReturn(page);

    Condition condition = new Condition();
    when(conditionMapper.toR4(canonical)).thenReturn(condition);

    IBundleProvider bundle = provider.searchConditions(null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(1);
    assertThat(bundle.getResources(0, 10)).containsExactly(condition);
    assertThat(bundle.getCurrentPageOffset()).isZero();
    assertThat(bundle.getCurrentPageSize()).isEqualTo(1);
    verify(patientService, never()).find(any(PatientSearchQuery.class));
    verify(encounterService, never()).find(any(EncounterSearchQuery.class));
  }

  @Test
  void searchConditions_includesSubjectAndEncounterResources() {
    ConditionSearchQuery query = ConditionSearchQuery.builder().build();
    when(conditionTranslator.translate(any(ConditionSearchCriteria.class))).thenReturn(query);

    ConditionCanonical canonical = mock(ConditionCanonical.class);
    when(canonical.getPatientUuid()).thenReturn("patient-123");
    when(canonical.getEncounterUuid()).thenReturn("encounter-456");
    Page<ConditionCanonical> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(canonical));
    when(page.getTotalElements()).thenReturn(1L);
    when(page.isEmpty()).thenReturn(false);
    when(conditionService.find(query)).thenReturn(page);

    Condition condition = new Condition();
    when(conditionMapper.toR4(canonical)).thenReturn(condition);

    LegacyPatientEntity patientEntity = mock(LegacyPatientEntity.class);
    Page<LegacyPatientEntity> patientPage = mock(Page.class);
    when(patientPage.getContent()).thenReturn(List.of(patientEntity));
    when(patientService.find(any(PatientSearchQuery.class))).thenReturn(patientPage);
    Patient patient = new Patient();
    when(legacyPatientMapper.toR4(patientEntity)).thenReturn(patient);

    FormEncounter formEncounter = mock(FormEncounter.class);
    Page<FormEncounter> encounterPage = mock(Page.class);
    when(encounterPage.getContent()).thenReturn(List.of(formEncounter));
    when(encounterService.find(any(EncounterSearchQuery.class))).thenReturn(encounterPage);
    Encounter encounter = new Encounter();
    when(encounterMapper.toR4(formEncounter)).thenReturn(encounter);

    Set<Include> includes = Set.of(new Include("Condition:subject"), new Include("Condition:encounter"));

    IBundleProvider bundle = provider.searchConditions(null, null, null, includes, null, null);

    assertThat(bundle.getResources(0, 10)).containsExactly(condition, patient, encounter);
    verify(patientService).find(any(PatientSearchQuery.class));
    verify(encounterService).find(any(EncounterSearchQuery.class));
  }

  @Test
  void searchConditions_returnsEmptyBundleWhenNoResults() {
    ConditionSearchQuery query = ConditionSearchQuery.builder().build();
    when(conditionTranslator.translate(any(ConditionSearchCriteria.class))).thenReturn(query);

    Page<ConditionCanonical> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
    when(page.isEmpty()).thenReturn(true);
    when(conditionService.find(query)).thenReturn(page);

    IBundleProvider bundle = provider.searchConditions(null, null, null, null, null, null);

    assertThat(bundle.size()).isZero();
    assertThat(bundle.getResources(0, 10)).isEmpty();
  }

  @Test
  void searchConditions_buildsCriteriaFromSearchParams() {
    ConditionSearchQuery query = ConditionSearchQuery.builder().build();
    when(conditionTranslator.translate(any(ConditionSearchCriteria.class))).thenReturn(query);

    Page<ConditionCanonical> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
    when(page.isEmpty()).thenReturn(true);
    when(conditionService.find(query)).thenReturn(page);

    ReferenceParam patient = new ReferenceParam("Patient/abc");
    TokenParam category = new TokenParam("problem-list-item");
    provider.searchConditions(patient, category, null, null, 10, 5);

    ArgumentCaptor<ConditionSearchCriteria> captor =
        ArgumentCaptor.forClass(ConditionSearchCriteria.class);
    verify(conditionTranslator).translate(captor.capture());
    assertThat(captor.getValue().getPatient()).isSameAs(patient);
    assertThat(captor.getValue().getCategory()).isSameAs(category);
    assertThat(captor.getValue().getCount()).isEqualTo(10);
    assertThat(captor.getValue().getOffset()).isEqualTo(5);
  }
}

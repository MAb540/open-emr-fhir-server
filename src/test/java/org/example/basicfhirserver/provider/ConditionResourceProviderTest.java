package org.example.basicfhirserver.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import org.example.basicfhirserver.query.resources.condition.ConditionSearchCriteria;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.example.basicfhirserver.query.translator.impl.ConditionTranslator;
import org.example.basicfhirserver.service.ConditionService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Condition;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConditionResourceProviderTest {

  @Mock private ConditionService conditionService;
  @Mock private ConditionTranslator conditionTranslator;

  @InjectMocks private ConditionResourceProvider provider;

  private static final UUID CONDITION_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void getResourceById_returnsConditionFromService() {
    IdType id = new IdType(CONDITION_UUID.toString());
    Condition condition = new Condition();
    when(conditionService.findById(CONDITION_UUID)).thenReturn(condition);

    Condition result = provider.getResourceById(id);

    assertThat(result).isSameAs(condition);
    verify(conditionService).findById(CONDITION_UUID);
  }

  @Test
  void getResourceById_propagatesNotFound() {
    IdType id = new IdType(CONDITION_UUID.toString());
    when(conditionService.findById(CONDITION_UUID))
        .thenThrow(new ResourceNotFoundException("not found"));

    assertThatThrownBy(() -> provider.getResourceById(id))
        .isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  void searchConditions_wrapsServiceResultInBundleProvider() {
    ConditionSearchQuery query = ConditionSearchQuery.builder().build();
    when(conditionTranslator.translate(any(ConditionSearchCriteria.class))).thenReturn(query);

    Condition primary = new Condition();
    Patient includedPatient = new Patient();
    SearchResult serviceResult =
        new SearchResult(List.of(primary), List.of(includedPatient), 2, 5);
    when(conditionService.find(query)).thenReturn(serviceResult);

    IBundleProvider bundle = provider.searchConditions(null, null, null, null, 10, 5);

    assertThat(bundle.size()).isEqualTo(2);
    assertThat(bundle.getResources(0, 10)).containsExactly(primary, includedPatient);
    assertThat(bundle.getCurrentPageOffset()).isEqualTo(5);
    assertThat(bundle.getCurrentPageSize()).isEqualTo(1);
    verify(conditionService).find(query);
  }

  @Test
  void searchConditions_returnsEmptyBundleWithZeroOffset() {
    ConditionSearchQuery query = ConditionSearchQuery.builder().build();
    when(conditionTranslator.translate(any(ConditionSearchCriteria.class))).thenReturn(query);
    SearchResult serviceResult = new SearchResult(List.of(), List.of(), 0, 0);
    when(conditionService.find(query)).thenReturn(serviceResult);

    IBundleProvider bundle = provider.searchConditions(null, null, null, null, null, null);

    assertThat(bundle.size()).isZero();
    assertThat(bundle.getResources(0, 10)).isEmpty();
    assertThat(bundle.getCurrentPageOffset()).isZero();
    assertThat(bundle.getCurrentPageSize()).isZero();
  }

  @Test
  void searchConditions_buildsCriteriaFromSearchParams() {
    ConditionSearchQuery query = ConditionSearchQuery.builder().build();
    when(conditionTranslator.translate(any(ConditionSearchCriteria.class))).thenReturn(query);
    SearchResult serviceResult = new SearchResult(List.of(), List.of(), 0, 5);
    when(conditionService.find(query)).thenReturn(serviceResult);

    ReferenceParam patient = new ReferenceParam("Patient/abc");
    TokenParam category = new TokenParam("problem-list-item");
    Set<Include> includes = Set.of(new Include("Condition:subject"));
    provider.searchConditions(patient, category, null, includes, 10, 5);

    ArgumentCaptor<ConditionSearchCriteria> captor =
        ArgumentCaptor.forClass(ConditionSearchCriteria.class);
    verify(conditionTranslator).translate(captor.capture());
    assertThat(captor.getValue().getPatient()).isSameAs(patient);
    assertThat(captor.getValue().getCategory()).isSameAs(category);
    assertThat(captor.getValue().getIncludes()).isSameAs(includes);
    assertThat(captor.getValue().getCount()).isEqualTo(10);
    assertThat(captor.getValue().getOffset()).isEqualTo(5);
  }
}

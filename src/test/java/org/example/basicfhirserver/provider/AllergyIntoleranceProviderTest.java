package org.example.basicfhirserver.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import java.util.List;
import java.util.Set;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchCriteria;
import org.example.basicfhirserver.query.resources.allergyintolerance.AllergyIntoleranceSearchQuery;
import org.example.basicfhirserver.query.translator.impl.AllergyIntoleranceSearchTranslator;
import org.example.basicfhirserver.service.AllergyIntoleranceService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.AllergyIntolerance;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AllergyIntoleranceProviderTest {

  @Mock private AllergyIntoleranceService allergyIntoleranceService;
  @Mock private AllergyIntoleranceSearchTranslator allergyIntoleranceSearchTranslator;

  @InjectMocks private AllergyIntoleranceProvider provider;

  @Test
  void searchAllergyIntolerance_wrapsServiceResultInBundleProvider() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();
    when(allergyIntoleranceSearchTranslator.translate(any(AllergyIntoleranceSearchCriteria.class)))
        .thenReturn(query);

    AllergyIntolerance primary = new AllergyIntolerance();
    Patient includedPatient = new Patient();
    SearchResult serviceResult =
        new SearchResult(List.of(primary), List.of(includedPatient), 2, 5);
    when(allergyIntoleranceService.search(query)).thenReturn(serviceResult);

    IBundleProvider bundle = provider.searchAllergyIntolerance(null, null, null, null, 10, 5);

    assertThat(bundle.size()).isEqualTo(2);
    assertThat(bundle.getResources(0, 10)).containsExactly(primary, includedPatient);
    assertThat(bundle.getCurrentPageOffset()).isEqualTo(5);
    assertThat(bundle.getCurrentPageSize()).isEqualTo(1);
    verify(allergyIntoleranceService).search(query);
  }

  @Test
  void searchAllergyIntolerance_returnsEmptyBundleWithZeroOffset() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();
    when(allergyIntoleranceSearchTranslator.translate(any(AllergyIntoleranceSearchCriteria.class)))
        .thenReturn(query);
    SearchResult serviceResult = new SearchResult(List.of(), List.of(), 0, 0);
    when(allergyIntoleranceService.search(query)).thenReturn(serviceResult);

    IBundleProvider bundle = provider.searchAllergyIntolerance(null, null, null, null, null, null);

    assertThat(bundle.size()).isZero();
    assertThat(bundle.getResources(0, 10)).isEmpty();
    assertThat(bundle.getCurrentPageOffset()).isZero();
    assertThat(bundle.getCurrentPageSize()).isZero();
  }

  @Test
  void searchAllergyIntolerance_buildsCriteriaFromSearchParams() {
    AllergyIntoleranceSearchQuery query = AllergyIntoleranceSearchQuery.builder().build();
    when(allergyIntoleranceSearchTranslator.translate(any(AllergyIntoleranceSearchCriteria.class)))
        .thenReturn(query);
    SearchResult serviceResult = new SearchResult(List.of(), List.of(), 5, 0);
    when(allergyIntoleranceService.search(query)).thenReturn(serviceResult);

    TokenParam id = new TokenParam("123");
    ReferenceParam patient = new ReferenceParam("Patient/abc");
    Set<Include> includes = Set.of(new Include("AllergyIntolerance:patient"));
    provider.searchAllergyIntolerance(id, patient, null, includes, 10, 5);

    ArgumentCaptor<AllergyIntoleranceSearchCriteria> captor =
        ArgumentCaptor.forClass(AllergyIntoleranceSearchCriteria.class);
    verify(allergyIntoleranceSearchTranslator).translate(captor.capture());
    assertThat(captor.getValue().getId()).isSameAs(id);
    assertThat(captor.getValue().getPatient()).isSameAs(patient);
    assertThat(captor.getValue().getIncludes()).isSameAs(includes);
    assertThat(captor.getValue().getCount()).isEqualTo(10);
    assertThat(captor.getValue().getOffset()).isEqualTo(5);
  }
}

package org.example.basicfhirserver.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchCriteria;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.example.basicfhirserver.query.translator.impl.PractitionerSearchTranslator;
import org.example.basicfhirserver.service.PractitionerService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PractitionerResourceProviderTest {

  @Mock private PractitionerService practitionerService;
  @Mock private PractitionerSearchTranslator practitionerSearchTranslator;

  @InjectMocks private PractitionerResourceProvider provider;

  private static final UUID PRACTITIONER_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void getResourceById_returnsPractitionerFromService() {
    IdType id = new IdType(PRACTITIONER_UUID.toString());
    Practitioner practitioner = new Practitioner();
    when(practitionerService.findById(PRACTITIONER_UUID)).thenReturn(practitioner);

    Practitioner result = provider.getResourceById(id);

    assertThat(result).isSameAs(practitioner);
    verify(practitionerService).findById(PRACTITIONER_UUID);
  }

  @Test
  void getResourceById_propagatesNotFound() {
    IdType id = new IdType(PRACTITIONER_UUID.toString());
    when(practitionerService.findById(PRACTITIONER_UUID))
        .thenThrow(new ResourceNotFoundException("not found"));

    assertThatThrownBy(() -> provider.getResourceById(id))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(practitionerService).findById(PRACTITIONER_UUID);
  }

  @Test
  void searchPractitioners_returnsPrimaryPractitioners() {
    PractitionerSearchQuery query = PractitionerSearchQuery.builder().build();
    when(practitionerSearchTranslator.translate(any(PractitionerSearchCriteria.class)))
        .thenReturn(query);

    Practitioner practitioner = new Practitioner();
    SearchResult searchResult = new SearchResult(List.of(practitioner), List.of(), 1, 0);
    when(practitionerService.search(query)).thenReturn(searchResult);

    IBundleProvider bundle = provider.searchPractitioners(null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(1);
    assertThat(bundle.getResources(0, 10)).containsExactly(practitioner);
    assertThat(bundle.getCurrentPageOffset()).isZero();
    assertThat(bundle.getCurrentPageSize()).isEqualTo(1);
  }

  @Test
  void searchPractitioners_returnsEmptyBundleWhenNoResults() {
    PractitionerSearchQuery query = PractitionerSearchQuery.builder().build();
    when(practitionerSearchTranslator.translate(any(PractitionerSearchCriteria.class)))
        .thenReturn(query);

    SearchResult searchResult = new SearchResult(List.of(), List.of(), 0, 0);
    when(practitionerService.search(query)).thenReturn(searchResult);

    IBundleProvider bundle = provider.searchPractitioners(null, null, null, null, null);

    assertThat(bundle.size()).isZero();
    assertThat(bundle.getResources(0, 10)).isEmpty();
  }

  @Test
  void searchPractitioners_buildsCriteriaFromSearchParams() {
    PractitionerSearchQuery query = PractitionerSearchQuery.builder().build();
    when(practitionerSearchTranslator.translate(any(PractitionerSearchCriteria.class)))
        .thenReturn(query);
    SearchResult searchResult = new SearchResult(List.of(), List.of(), 0, 0);
    when(practitionerService.search(query)).thenReturn(searchResult);

    TokenParam id = new TokenParam("123");
    provider.searchPractitioners(id, null, null, 10, 5);

    ArgumentCaptor<PractitionerSearchCriteria> captor =
        ArgumentCaptor.forClass(PractitionerSearchCriteria.class);
    verify(practitionerSearchTranslator).translate(captor.capture());
    assertThat(captor.getValue().getId()).isSameAs(id);
    assertThat(captor.getValue().getCount()).isEqualTo(10);
    assertThat(captor.getValue().getOffset()).isEqualTo(5);
  }
}

package org.example.basicfhirserver.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchCriteria;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.translator.impl.ObservationSearchTranslator;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ObservationResourceProviderTest {

  @Mock private ObservationService observationService;
  @Mock private ObservationSearchTranslator observationSearchTranslator;

  @InjectMocks private ObservationResourceProvider provider;

  private static final UUID OBSERVATION_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID PATIENT_UUID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");

  @Test
  void getResourceById_returnsObservationFromService() {
    IdType id = new IdType(OBSERVATION_UUID.toString());
    Observation observation = new Observation();
    when(observationService.findById(OBSERVATION_UUID)).thenReturn(observation);

    Observation result = provider.getResourceById(id);

    assertThat(result).isSameAs(observation);
    verify(observationService).findById(OBSERVATION_UUID);
  }

  @Test
  void getResourceById_propagatesNotFound() {
    IdType id = new IdType(OBSERVATION_UUID.toString());
    when(observationService.findById(OBSERVATION_UUID))
        .thenThrow(new ResourceNotFoundException("not found"));

    assertThatThrownBy(() -> provider.getResourceById(id))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(observationService).findById(OBSERVATION_UUID);
  }

  @Test
  void searchObservations_returnsPrimaryObservations() {
    ObservationSearchQuery query = ObservationSearchQuery.builder().build();
    when(observationSearchTranslator.translate(any(ObservationSearchCriteria.class)))
        .thenReturn(query);

    Observation observation = new Observation();
    SearchResult searchResult = new SearchResult(List.of(observation), List.of(), 1, 0);
    when(observationService.search(query)).thenReturn(searchResult);

    IBundleProvider bundle =
        provider.searchObservations(null, null, null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(1);
    assertThat(bundle.getResources(0, 10)).containsExactly(observation);
    assertThat(bundle.getCurrentPageOffset()).isZero();
    assertThat(bundle.getCurrentPageSize()).isEqualTo(1);
  }

  @Test
  void searchObservations_returnsIncludedResourcesFromService() {
    ObservationSearchQuery query = ObservationSearchQuery.builder().build();
    when(observationSearchTranslator.translate(any(ObservationSearchCriteria.class)))
        .thenReturn(query);

    Observation observation = new Observation();
    Patient patient = new Patient();
    SearchResult searchResult = new SearchResult(List.of(observation), List.of(patient), 2, 0);
    when(observationService.search(query)).thenReturn(searchResult);

    Set<Include> includes = Set.of(new Include("Observation:patient"));

    IBundleProvider bundle =
        provider.searchObservations(null, null, null, null, null, includes, null, null);

    assertThat(bundle.size()).isEqualTo(2);
    assertThat(bundle.getResources(0, 10)).containsExactly(observation, patient);
    verify(observationService).search(query);
  }

  @Test
  void searchObservations_returnsEmptyBundleWhenNoResults() {
    ObservationSearchQuery query = ObservationSearchQuery.builder().build();
    when(observationSearchTranslator.translate(any(ObservationSearchCriteria.class)))
        .thenReturn(query);

    SearchResult searchResult = new SearchResult(List.of(), List.of(), 0, 0);
    when(observationService.search(query)).thenReturn(searchResult);

    IBundleProvider bundle =
        provider.searchObservations(null, null, null, null, null, null, null, null);

    assertThat(bundle.size()).isZero();
    assertThat(bundle.getResources(0, 10)).isEmpty();
  }

  @Test
  void searchObservations_buildsCriteriaFromSearchParams() {
    ObservationSearchQuery query = ObservationSearchQuery.builder().build();
    when(observationSearchTranslator.translate(any(ObservationSearchCriteria.class)))
        .thenReturn(query);
    SearchResult searchResult = new SearchResult(List.of(), List.of(), 0, 0);
    when(observationService.search(query)).thenReturn(searchResult);

    ReferenceParam patient = new ReferenceParam().setValue("Patient/" + PATIENT_UUID);
    Set<Include> includes = Set.of(new Include("Observation:encounter"));

    provider.searchObservations(patient, null, null, null, null, includes, 10, 5);

    ArgumentCaptor<ObservationSearchCriteria> captor =
        ArgumentCaptor.forClass(ObservationSearchCriteria.class);
    verify(observationSearchTranslator).translate(captor.capture());
    assertThat(captor.getValue().getPatient()).isSameAs(patient);
    assertThat(captor.getValue().getCount()).isEqualTo(10);
    assertThat(captor.getValue().getOffset()).isEqualTo(5);
    assertThat(captor.getValue().getIncludes()).isEqualTo(includes);
  }
}

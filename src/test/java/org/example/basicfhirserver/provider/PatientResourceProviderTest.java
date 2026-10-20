package org.example.basicfhirserver.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.example.basicfhirserver.jobs.export.ExportService;
import org.example.basicfhirserver.jobs.export.FhirExportServiceConstants;
import org.example.basicfhirserver.query.resources.patient.PatientSearchCriteria;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.query.translator.impl.PatientSearchTranslator;
import org.example.basicfhirserver.service.PatientService;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;
import org.jobrunr.jobs.JobId;
import org.jobrunr.jobs.lambdas.JobLambda;
import org.jobrunr.scheduling.JobScheduler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatientResourceProviderTest {

  @Mock private PatientService patientService;
  @Mock private PatientSearchTranslator patientSearchTranslator;
  @Mock private ExportService exportService;
  @Mock private JobScheduler jobScheduler;

  @InjectMocks private PatientResourceProvider provider;

  private static final UUID PATIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void getResourceById_returnsMappedPatient() {
    IdType id = new IdType(PATIENT_UUID.toString());
    Patient patient = new Patient();
    when(patientService.findById(PATIENT_UUID)).thenReturn(patient);

    Patient result = provider.getResourceById(id);

    assertThat(result).isSameAs(patient);
    verify(patientService).findById(PATIENT_UUID);
  }

  @Test
  void getResourceById_propagatesNotFound() {
    IdType id = new IdType(PATIENT_UUID.toString());
    when(patientService.findById(PATIENT_UUID))
        .thenThrow(new ResourceNotFoundException("not found"));

    assertThatThrownBy(() -> provider.getResourceById(id))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(patientService).findById(PATIENT_UUID);
  }

  @Test
  void searchPatients_returnsPrimaryPatientsWithoutIncludes() {
    PatientSearchQuery query = PatientSearchQuery.builder().build();
    when(patientSearchTranslator.translate(any(PatientSearchCriteria.class))).thenReturn(query);

    Patient patient = new Patient();
    SearchResult searchResult = new SearchResult(List.of(patient), List.of(), 1, 0);
    when(patientService.search(query)).thenReturn(searchResult);

    IBundleProvider bundle =
        provider.searchPatients(
            null, null, null, null, null, null, null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(1);
    assertThat(bundle.getResources(0, 10)).containsExactly(patient);
    assertThat(bundle.getCurrentPageOffset()).isZero();
    assertThat(bundle.getCurrentPageSize()).isEqualTo(1);
  }

  @Test
  void searchPatients_returnsIncludedResourcesFromService() {
    PatientSearchQuery query = PatientSearchQuery.builder().build();
    when(patientSearchTranslator.translate(any(PatientSearchCriteria.class))).thenReturn(query);

    Patient patient = new Patient();
    Observation observation = new Observation();
    Encounter encounter = new Encounter();
    SearchResult searchResult =
        new SearchResult(List.of(patient), List.of(observation, encounter), 3, 0);
    when(patientService.search(query)).thenReturn(searchResult);

    Set<Include> revIncludes =
        Set.of(new Include("Observation:patient"), new Include("Encounter:patient"));

    IBundleProvider bundle =
        provider.searchPatients(
            null, null, null, null, null, null, null, null, null, revIncludes, null, null);

    assertThat(bundle.size()).isEqualTo(3);
    assertThat(bundle.getResources(0, 10)).containsExactly(patient, observation, encounter);
    verify(patientService).search(query);
  }

  @Test
  void searchPatients_returnsEmptyBundleWhenNoResults() {
    PatientSearchQuery query = PatientSearchQuery.builder().build();
    when(patientSearchTranslator.translate(any(PatientSearchCriteria.class))).thenReturn(query);

    SearchResult searchResult = new SearchResult(List.of(), List.of(), 0, 0);
    when(patientService.search(query)).thenReturn(searchResult);

    IBundleProvider bundle =
        provider.searchPatients(
            null, null, null, null, null, null, null, null, null, null, null, null);

    assertThat(bundle.size()).isZero();
    assertThat(bundle.getResources(0, 10)).isEmpty();
  }

  @Test
  void searchPatients_buildsCriteriaFromSearchParams() {
    PatientSearchQuery query = PatientSearchQuery.builder().build();
    when(patientSearchTranslator.translate(any(PatientSearchCriteria.class))).thenReturn(query);
    SearchResult searchResult = new SearchResult(List.of(), List.of(), 0, 0);
    when(patientService.search(query)).thenReturn(searchResult);

    TokenParam id = new TokenParam("123");
    Set<Include> revIncludes = Set.of(new Include("Observation:patient"));
    provider.searchPatients(id, null, null, null, null, null, null, null, null, revIncludes, 10, 5);

    ArgumentCaptor<PatientSearchCriteria> captor =
        ArgumentCaptor.forClass(PatientSearchCriteria.class);
    verify(patientSearchTranslator).translate(captor.capture());
    assertThat(captor.getValue().getId()).isSameAs(id);
    assertThat(captor.getValue().getCount()).isEqualTo(10);
    assertThat(captor.getValue().getOffset()).isEqualTo(5);
    assertThat(captor.getValue().getRevIncludes()).isEqualTo(revIncludes);
  }

  @Test
  void patientExport_enqueuesJobAndReturnsAccepted() throws Exception {
    when(exportService.supportedPatientExportResources())
        .thenReturn(List.of("Patient", "Observation", "Encounter"));

    RequestDetails details = org.mockito.Mockito.mock(RequestDetails.class);
    when(details.getHeader("Prefer")).thenReturn("respond-async");
    when(details.getParameters()).thenReturn(Map.<String, String[]>of());
    when(details.getFhirServerBase()).thenReturn("http://localhost:8080/fhir");

    HttpServletResponse response = org.mockito.Mockito.mock(HttpServletResponse.class);
    when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

    JobId jobId = org.mockito.Mockito.mock(JobId.class);
    when(jobId.toString()).thenReturn("job-123");
    when(jobScheduler.enqueue(any(JobLambda.class))).thenReturn(jobId);

    provider.patientExport(details, response);

    verify(response).setStatus(HttpServletResponse.SC_ACCEPTED);
    verify(response)
        .setHeader(
            "Content-Location",
            "http://localhost:8080/fhir"
                + FhirExportServiceConstants.ExportPollEndpointWithParams
                + "job-123");
    verify(jobScheduler).enqueue(any(JobLambda.class));
  }

  @Test
  void patientExport_returnsBadRequestWhenPreferHeaderMissing() throws Exception {
    when(exportService.supportedPatientExportResources())
        .thenReturn(List.of("Patient", "Observation", "Encounter"));

    RequestDetails details = org.mockito.Mockito.mock(RequestDetails.class);
    when(details.getHeader("Prefer")).thenReturn(null);

    HttpServletResponse response = org.mockito.Mockito.mock(HttpServletResponse.class);
    when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

    provider.patientExport(details, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(jobScheduler, never()).enqueue(any(JobLambda.class));
  }
}

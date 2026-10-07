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
import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.jobs.export.FhirExportService;
import org.example.basicfhirserver.jobs.export.FhirExportServiceConstants;
import org.example.basicfhirserver.mapper.EncounterMapper;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.mapper.ObservationMapper;
import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.model.VitalObservation;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.query.resources.patient.PatientSearchCriteria;
import org.example.basicfhirserver.query.resources.patient.PatientSearchQuery;
import org.example.basicfhirserver.query.translator.impl.PatientSearchTranslator;
import org.example.basicfhirserver.service.EncounterService;
import org.example.basicfhirserver.service.ObservationService;
import org.example.basicfhirserver.service.PatientService;
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
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class PatientResourceProviderTest {

  @Mock private LegacyPatientMapper legacyPatientMapper;
  @Mock private PatientSearchTranslator patientSearchTranslator;
  @Mock private PatientService patientService;
  @Mock private ObservationService observationService;
  @Mock private ObservationMapper observationMapper;
  @Mock private EncounterService encounterService;
  @Mock private EncounterMapper encounterMapper;
  @Mock private FhirExportService fhirExportService;
  @Mock private JobScheduler jobScheduler;

  @InjectMocks private PatientResourceProvider provider;

  private static final UUID PATIENT_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void getResourceById_returnsMappedPatient() {
    IdType id = new IdType(PATIENT_UUID.toString());
    LegacyPatientEntity entity = mock(LegacyPatientEntity.class);
    Patient patient = new Patient();
    when(patientService.findById(PATIENT_UUID)).thenReturn(entity);
    when(legacyPatientMapper.toR4(entity)).thenReturn(patient);

    Patient result = provider.getResourceById(id);

    assertThat(result).isSameAs(patient);
    verify(patientService).findById(PATIENT_UUID);
    verify(legacyPatientMapper).toR4(entity);
  }

  @Test
  void getResourceById_propagatesNotFound() {
    IdType id = new IdType(PATIENT_UUID.toString());
    when(patientService.findById(PATIENT_UUID))
        .thenThrow(new ResourceNotFoundException("not found"));

    assertThatThrownBy(() -> provider.getResourceById(id))
        .isInstanceOf(ResourceNotFoundException.class);

    verify(legacyPatientMapper, never()).toR4(any(LegacyPatientEntity.class));
  }

  @Test
  void searchPatients_returnsMappedPatientsWithoutIncludes() {
    PatientSearchQuery query = PatientSearchQuery.builder().build();
    when(patientSearchTranslator.translate(any(PatientSearchCriteria.class))).thenReturn(query);

    LegacyPatientEntity entity = mock(LegacyPatientEntity.class);
    when(entity.getUuid()).thenReturn(PATIENT_UUID);
    Page<LegacyPatientEntity> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(entity));
    when(page.getTotalElements()).thenReturn(1L);
    when(page.isEmpty()).thenReturn(false);
    when(patientService.find(query)).thenReturn(page);

    Patient patient = new Patient();
    when(legacyPatientMapper.toR4(entity)).thenReturn(patient);

    IBundleProvider bundle =
        provider.searchPatients(
            null, null, null, null, null, null, null, null, null, null, null, null);

    assertThat(bundle.size()).isEqualTo(1);
    assertThat(bundle.getResources(0, 10)).containsExactly(patient);
    assertThat(bundle.getCurrentPageOffset()).isZero();
    assertThat(bundle.getCurrentPageSize()).isEqualTo(1);
    verify(observationService, never()).find(any(ObservationSearchQuery.class));
    verify(encounterService, never()).find(any(EncounterSearchQuery.class));
  }

  @Test
  void searchPatients_includesReverseIncludedResources() {
    PatientSearchQuery query = PatientSearchQuery.builder().build();
    when(patientSearchTranslator.translate(any(PatientSearchCriteria.class))).thenReturn(query);

    LegacyPatientEntity entity = mock(LegacyPatientEntity.class);
    when(entity.getUuid()).thenReturn(PATIENT_UUID);
    Page<LegacyPatientEntity> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(entity));
    when(page.getTotalElements()).thenReturn(1L);
    when(page.isEmpty()).thenReturn(false);
    when(patientService.find(query)).thenReturn(page);

    Patient patient = new Patient();
    when(legacyPatientMapper.toR4(entity)).thenReturn(patient);

    VitalObservation vitalObservation = mock(VitalObservation.class);
    when(observationService.find(any(ObservationSearchQuery.class)))
        .thenReturn(List.of(vitalObservation));
    Observation observation = new Observation();
    when(observationMapper.toR4(vitalObservation)).thenReturn(observation);

    FormEncounter formEncounter = mock(FormEncounter.class);
    Page<FormEncounter> encounterPage = mock(Page.class);
    when(encounterPage.getContent()).thenReturn(List.of(formEncounter));
    when(encounterService.find(any(EncounterSearchQuery.class))).thenReturn(encounterPage);
    Encounter encounter = new Encounter();
    when(encounterMapper.toR4(formEncounter)).thenReturn(encounter);

    Set<Include> revIncludes =
        Set.of(new Include("Observation:patient"), new Include("Encounter:patient"));

    IBundleProvider bundle =
        provider.searchPatients(
            null, null, null, null, null, null, null, null, null, revIncludes, null, null);

    assertThat(bundle.getResources(0, 10)).containsExactly(patient, observation, encounter);
    verify(observationService).find(any(ObservationSearchQuery.class));
    verify(encounterService).find(any(EncounterSearchQuery.class));
  }

  @Test
  void searchPatients_returnsEmptyBundleWhenNoResults() {
    PatientSearchQuery query = PatientSearchQuery.builder().build();
    when(patientSearchTranslator.translate(any(PatientSearchCriteria.class))).thenReturn(query);

    Page<LegacyPatientEntity> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
    when(page.isEmpty()).thenReturn(true);
    when(patientService.find(query)).thenReturn(page);

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

    Page<LegacyPatientEntity> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
    when(page.isEmpty()).thenReturn(true);
    when(patientService.find(query)).thenReturn(page);

    TokenParam id = new TokenParam("123");
    provider.searchPatients(id, null, null, null, null, null, null, null, null, null, 10, 5);

    ArgumentCaptor<PatientSearchCriteria> captor =
        ArgumentCaptor.forClass(PatientSearchCriteria.class);
    verify(patientSearchTranslator).translate(captor.capture());
    assertThat(captor.getValue().getId()).isSameAs(id);
    assertThat(captor.getValue().getCount()).isEqualTo(10);
    assertThat(captor.getValue().getOffset()).isEqualTo(5);
  }

  @Test
  void patientExport_enqueuesJobAndReturnsAccepted() throws Exception {
    when(fhirExportService.supportedExportResources())
        .thenReturn(List.of("Patient", "Observation", "Encounter"));

    RequestDetails details = mock(RequestDetails.class);
    when(details.getHeader("Prefer")).thenReturn("respond-async");
    when(details.getParameters()).thenReturn(Map.<String, String[]>of());
    when(details.getFhirServerBase()).thenReturn("http://localhost:8080/fhir");

    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

    JobId jobId = mock(JobId.class);
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
    when(fhirExportService.supportedExportResources())
        .thenReturn(List.of("Patient", "Observation", "Encounter"));

    RequestDetails details = mock(RequestDetails.class);
    when(details.getHeader("Prefer")).thenReturn(null);

    HttpServletResponse response = mock(HttpServletResponse.class);
    when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

    provider.patientExport(details, response);

    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
    verify(jobScheduler, never()).enqueue(any(JobLambda.class));
  }
}

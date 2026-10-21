package org.example.basicfhirserver.service.impl.medicationRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.MedicationRequestMapper;
import org.example.basicfhirserver.mapper.impl.MedicationRequestMapperImpl;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.query.resources.medicationrequest.MedicationRequestSearchQuery;
import org.example.basicfhirserver.repository.jdbc.prescription.FacilityDBRecord;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionDBRecord;
import org.example.basicfhirserver.repository.jdbc.prescription.PrescriptionRepository;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class MedicationRequestServiceImplTest {

  @Mock private PrescriptionRepository prescriptionRepository;

  // The mapper is a pure function over a DB record - there is no I/O to mock, so the real
  // implementation is used and the assertions target the returned FHIR resource.
  private final MedicationRequestMapper medicationRequestMapper = new MedicationRequestMapperImpl();

  private MedicationRequestServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new MedicationRequestServiceImpl(prescriptionRepository, medicationRequestMapper);
  }

  private static final UUID PRESCRIPTION_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID PATIENT_UUID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
  private static final UUID ENCOUNTER_UUID =
      UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
  private static final UUID PRACTITIONER_UUID =
      UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
  private static final UUID FACILITY_UUID = UUID.fromString("523e4567-e89b-12d3-a456-426614174004");
  private static final LocalDateTime DATE_ADDED = LocalDateTime.of(2026, 2, 1, 8, 0);

  private static PrescriptionDBRecord record(UUID pruuid) {
    return PrescriptionDBRecord.builder()
        .uuid(PRESCRIPTION_UUID)
        .drug("Metformin 500mg")
        .rxnormDrugcode("860975")
        .status("active")
        .intent("order")
        .category("community")
        .categoryTitle("Home/Community")
        .note("Take with food")
        .dateAdded(DATE_ADDED)
        .dateModified(LocalDateTime.of(2026, 2, 2, 9, 0))
        .puuid(PATIENT_UUID)
        .euuid(ENCOUNTER_UUID)
        .pruuid(pruuid)
        .quantity("30")
        .unitTitle("tablet")
        .prescriptionDrugSize("1")
        .drugDosageInstructions("1 tablet daily")
        .reportingSourceType("user")
        .reportingSourceUuid(PRACTITIONER_UUID.toString())
        .build();
  }

  @Test
  void findById_returnsMappedMedicationRequest() {
    when(prescriptionRepository.findById(PRESCRIPTION_UUID))
        .thenReturn(List.of(record(PRACTITIONER_UUID)));

    MedicationRequest result = service.findById(PRESCRIPTION_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(PRESCRIPTION_UUID.toString());
    assertThat(result.getStatus()).isEqualTo(MedicationRequest.MedicationRequestStatus.ACTIVE);
    assertThat(result.getIntent()).isEqualTo(MedicationRequest.MedicationRequestIntent.ORDER);
    assertThat(((Reference) result.getSubject()).getReference())
        .isEqualTo("Patient/" + PATIENT_UUID);
    assertThat(result.getEncounter().getReference()).isEqualTo("Encounter/" + ENCOUNTER_UUID);
    assertThat(result.getRequester().getReference()).isEqualTo("Practitioner/" + PRACTITIONER_UUID);
    assertThat(((Reference) result.getReported()).getReference())
        .isEqualTo("Practitioner/" + PRACTITIONER_UUID);
    assertThat(((CodeableConcept) result.getMedication()).getCodingFirstRep().getCode())
        .isEqualTo("860975");
    assertThat(((CodeableConcept) result.getMedication()).getCodingFirstRep().getDisplay())
        .isEqualTo("Metformin 500mg");
    assertThat(result.getCategory().get(0).getCodingFirstRep().getCode()).isEqualTo("community");
    assertThat(result.getNote().get(0).getText()).isEqualTo("Take with food");
    assertThat(result.getDosageInstruction().get(0).getText()).isEqualTo("1 tablet daily");
    assertThat(result.getDispenseRequest().getQuantity().getValue().intValue()).isEqualTo(30);
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(ProfilesConstants.HL7_US_CORE_MEDICATIONREQUEST);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecords() {
    when(prescriptionRepository.findById(PRESCRIPTION_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(PRESCRIPTION_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(PRESCRIPTION_UUID.toString());
  }

  @Test
  void search_returnsMedicationRequestsWithTheFacilityAsRequester() {
    MedicationRequestSearchQuery query = MedicationRequestSearchQuery.builder().build();

    Page<PrescriptionDBRecord> page = new PageImpl<>(List.of(record(null)));

    when(prescriptionRepository.find(query)).thenReturn(page);
    when(prescriptionRepository.findFacility())
        .thenReturn(List.of(FacilityDBRecord.builder().id(1L).uuid(FACILITY_UUID).build()));

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(1);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.currentOffset()).isZero();

    MedicationRequest medicationRequest = (MedicationRequest) result.primaryResources().get(0);
    assertThat(medicationRequest.getId()).isEqualTo(PRESCRIPTION_UUID.toString());
    assertThat(medicationRequest.getRequester().getReference())
        .isEqualTo("Organization/" + FACILITY_UUID);
    assertThat(medicationRequest.getSubject().getReference()).isEqualTo("Patient/" + PATIENT_UUID);
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    MedicationRequestSearchQuery query = MedicationRequestSearchQuery.builder().build();

    Page<PrescriptionDBRecord> page = new PageImpl<>(List.of());

    when(prescriptionRepository.find(query)).thenReturn(page);
    when(prescriptionRepository.findFacility()).thenReturn(List.of());

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    MedicationRequestSearchQuery query =
        MedicationRequestSearchQuery.builder().offset(10).count(5).build();

    Page<PrescriptionDBRecord> page = new PageImpl<>(List.of());

    when(prescriptionRepository.find(query)).thenReturn(page);
    when(prescriptionRepository.findFacility()).thenReturn(List.of());

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
  }
}

package org.example.basicfhirserver.service.impl.immunization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.ImmunizationMapper;
import org.example.basicfhirserver.mapper.impl.ImmunizationMapperImpl;
import org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.query.resources.immunization.ImmunizationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.immunization.ImmunizationDBRecord;
import org.example.basicfhirserver.repository.jdbc.immunization.ImmunizationRepository;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Immunization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class ImmunizationServiceImplTest {

  @Mock private ImmunizationRepository immunizationRepository;

  // The mapper is a pure function over a DB record - there is no I/O to mock, so the real
  // implementation is used and the assertions target the returned FHIR resource.
  private final ImmunizationMapper immunizationMapper = new ImmunizationMapperImpl();

  private ImmunizationServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new ImmunizationServiceImpl(immunizationRepository, immunizationMapper);
  }

  private static final UUID IMMUNIZATION_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID SECOND_IMMUNIZATION_UUID =
      UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
  private static final UUID PATIENT_UUID = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
  private static final UUID PRACTITIONER_UUID =
      UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
  private static final UUID ENCOUNTER_UUID =
      UUID.fromString("523e4567-e89b-12d3-a456-426614174004");

  private static ImmunizationDBRecord record(UUID uuid, String cvxCode, String cvxCodeText) {
    return ImmunizationDBRecord.builder()
        .id(1L)
        .uuid(uuid)
        .patientPuuid(PATIENT_UUID)
        .administeredDate(LocalDateTime.of(2026, 1, 15, 9, 0))
        .cvxCode(cvxCode)
        .cvxCodeText(cvxCodeText)
        .manufacturer("Acme Vaccines")
        .lotNumber("LOT-123")
        .completionStatus("Completed")
        .primarySource(true)
        .amountAdministered(1)
        .amountAdministeredUnit("mL")
        .siteCode("LA")
        .siteDisplay("Left arm")
        .providerUuid(PRACTITIONER_UUID)
        .administeredBy("dr.smith")
        .note("Routine flu shot")
        .encounterEuuid(ENCOUNTER_UUID)
        .expirationDate(LocalDate.of(2026, 6, 1))
        .createDate(LocalDateTime.of(2026, 1, 16, 8, 0))
        .build();
  }

  @Test
  void findById_returnsMappedImmunization() {
    when(immunizationRepository.findById(IMMUNIZATION_UUID))
        .thenReturn(List.of(record(IMMUNIZATION_UUID, "140", "Influenza, seasonal, injectable")));

    Immunization result = service.findById(IMMUNIZATION_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(IMMUNIZATION_UUID.toString());
    assertThat(result.getStatus()).isEqualTo(Immunization.ImmunizationStatus.COMPLETED);
    assertThat(result.getVaccineCode().getCodingFirstRep().getSystem())
        .isEqualTo(FhirCodeSystemConstants.CVX);
    assertThat(result.getVaccineCode().getCodingFirstRep().getCode()).isEqualTo("140");
    assertThat(result.getVaccineCode().getCodingFirstRep().getDisplay())
        .isEqualTo("Influenza, seasonal, injectable");
    assertThat(result.getPatient().getReference()).isEqualTo("Patient/" + PATIENT_UUID);
    assertThat(result.getEncounter().getReference()).isEqualTo("Encounter/" + ENCOUNTER_UUID);
    assertThat(result.getOccurrence()).isNotNull();
    assertThat(result.getPrimarySource()).isTrue();
    assertThat(result.getLotNumber()).isEqualTo("LOT-123");
    assertThat(result.getManufacturer().getDisplay()).isEqualTo("Acme Vaccines");
    assertThat(result.getSite().getCodingFirstRep().getSystem())
        .isEqualTo(FhirCodeSystemConstants.HL7_V3_ACT_SITE);
    assertThat(result.getSite().getCodingFirstRep().getCode()).isEqualTo("LA");
    assertThat(result.getDoseQuantity().getValue()).isEqualByComparingTo(new BigDecimal("1"));
    assertThat(result.getDoseQuantity().getSystem())
        .isEqualTo(FhirCodeSystemConstants.UNITS_OF_MEASURE);
    assertThat(result.getDoseQuantity().getCode()).isEqualTo("mL");
    assertThat(result.getPerformerFirstRep().getActor().getReference())
        .isEqualTo("Practitioner/" + PRACTITIONER_UUID);
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(ProfilesConstants.HL7_US_CORE_IMMUNIZATION);
  }

  @Test
  void findById_mapsRefusedImmunizationAsNotDone() {
    ImmunizationDBRecord refused =
        ImmunizationDBRecord.builder()
            .id(2L)
            .uuid(IMMUNIZATION_UUID)
            .patientPuuid(PATIENT_UUID)
            .cvxCode("140")
            .cvxCodeText("Influenza, seasonal, injectable")
            .completionStatus("Refused")
            .primarySource(false)
            .refusalReason("religious_exemption")
            .refusalReasonCdcNipCode("01")
            .refusalReasonDescription("Religious exemption")
            .build();
    when(immunizationRepository.findById(IMMUNIZATION_UUID)).thenReturn(List.of(refused));

    Immunization result = service.findById(IMMUNIZATION_UUID);

    assertThat(result.getStatus()).isEqualTo(Immunization.ImmunizationStatus.NOTDONE);
    assertThat(result.getStatusReason().getCodingFirstRep().getSystem())
        .isEqualTo(FhirCodeSystemConstants.IMMUNIZATION_OBJECTION_REASON);
    assertThat(result.getStatusReason().getCodingFirstRep().getCode()).isEqualTo("RELIG");
    assertThat(result.getStatusReason().getCodingFirstRep().getDisplay())
        .isEqualTo("religious objection");
  }

  @Test
  void findById_throwsNotFoundWhenNoRecord() {
    when(immunizationRepository.findById(IMMUNIZATION_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(IMMUNIZATION_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(IMMUNIZATION_UUID.toString());
  }

  @Test
  void search_returnsImmunizationsForEveryRecordOnThePage() {
    ImmunizationSearchQuery query = ImmunizationSearchQuery.builder().build();

    Page<ImmunizationDBRecord> page =
        new PageImpl<>(
            List.of(
                record(IMMUNIZATION_UUID, "140", "Influenza, seasonal, injectable"),
                record(SECOND_IMMUNIZATION_UUID, "08", "Hep B, adolescent or pediatric")));

    when(immunizationRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(2);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.currentOffset()).isZero();

    Immunization first = (Immunization) result.primaryResources().get(0);
    assertThat(first.getId()).isEqualTo(IMMUNIZATION_UUID.toString());
    assertThat(first.getVaccineCode().getCodingFirstRep().getCode()).isEqualTo("140");

    Immunization second = (Immunization) result.primaryResources().get(1);
    assertThat(second.getId()).isEqualTo(SECOND_IMMUNIZATION_UUID.toString());
    assertThat(second.getVaccineCode().getCodingFirstRep().getCode()).isEqualTo("08");
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    ImmunizationSearchQuery query = ImmunizationSearchQuery.builder().build();

    when(immunizationRepository.find(query)).thenReturn(new PageImpl<>(List.of()));

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    ImmunizationSearchQuery query = ImmunizationSearchQuery.builder().offset(10).count(5).build();

    when(immunizationRepository.find(query)).thenReturn(new PageImpl<>(List.of()));

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
  }
}

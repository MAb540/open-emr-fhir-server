package org.example.basicfhirserver.service.impl.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.EncounterMapper;
import org.example.basicfhirserver.mapper.impl.EncounterMapperImpl;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.repository.jdbc.formencounter.FormEncounterDBRecord;
import org.example.basicfhirserver.repository.jdbc.formencounter.FormEncounterRepository;
import org.example.basicfhirserver.service.assembler.formencounter.FormEncounterAssembler;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class EncounterServiceImplTest {

  @Mock private FormEncounterRepository formEncounterRepository;

  // The assembler and the mapper are pure functions over a DB record - there is no I/O to
  // mock, so the real implementations are used and the assertions target the returned resource.
  private final FormEncounterAssembler formEncounterAssembler = new FormEncounterAssembler();
  private final EncounterMapper encounterMapper = new EncounterMapperImpl();

  private EncounterServiceImpl service;

  @BeforeEach
  void setUp() {
    service =
        new EncounterServiceImpl(formEncounterRepository, formEncounterAssembler, encounterMapper);
  }

  private static final UUID ENCOUNTER_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID PATIENT_UUID = UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
  private static final LocalDateTime ENCOUNTER_DATE = LocalDateTime.of(2026, 1, 15, 10, 30);

  private static FormEncounterDBRecord record(UUID uuid, String classCode, String classTitle) {
    return FormEncounterDBRecord.builder()
        .eid(1L)
        .euuid(uuid)
        .pid(42L)
        .puuid(PATIENT_UUID)
        .date(ENCOUNTER_DATE)
        .lastUpdate(LocalDateTime.of(2026, 1, 16, 8, 0))
        .classCode(classCode)
        .classTitle(classTitle)
        .reason("Routine check up")
        .build();
  }

  @Test
  void findById_returnsMappedEncounter() {
    when(formEncounterRepository.findById(ENCOUNTER_UUID))
        .thenReturn(List.of(record(ENCOUNTER_UUID, "AMB", "Ambulatory")));

    Encounter result = service.findById(ENCOUNTER_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(ENCOUNTER_UUID.toString());
    assertThat(result.getStatus()).isEqualTo(Encounter.EncounterStatus.FINISHED);
    assertThat(result.getClass_().getCode()).isEqualTo("AMB");
    assertThat(result.getClass_().getDisplay()).isEqualTo("Ambulatory");
    assertThat(result.getSubject().getReference()).isEqualTo("Patient/" + PATIENT_UUID);
    assertThat(result.getPeriod().getStart())
        .isEqualTo(Date.from(ENCOUNTER_DATE.atZone(ZoneId.systemDefault()).toInstant()));
    assertThat(result.getReasonCode().get(0).getText()).isEqualTo("Routine check up");
    assertThat(result.getIdentifier().get(0).getValue()).isEqualTo(ENCOUNTER_UUID.toString());
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(ProfilesConstants.HL7_US_CORE_ENCOUNTER);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecords() {
    when(formEncounterRepository.findById(ENCOUNTER_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(ENCOUNTER_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(ENCOUNTER_UUID.toString());
  }

  @Test
  void search_returnsEncountersForEveryRecordOnThePage() {
    EncounterSearchQuery query = EncounterSearchQuery.builder().build();

    UUID secondEncounterUuid = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
    Page<FormEncounterDBRecord> page =
        new PageImpl<>(
            List.of(
                record(ENCOUNTER_UUID, "AMB", "Ambulatory"),
                record(secondEncounterUuid, "IMP", "Inpatient")));

    when(formEncounterRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(2);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.currentOffset()).isZero();

    Encounter first = (Encounter) result.primaryResources().get(0);
    assertThat(first.getId()).isEqualTo(ENCOUNTER_UUID.toString());
    assertThat(first.getClass_().getCode()).isEqualTo("AMB");

    Encounter second = (Encounter) result.primaryResources().get(1);
    assertThat(second.getId()).isEqualTo(secondEncounterUuid.toString());
    assertThat(second.getClass_().getCode()).isEqualTo("IMP");
    assertThat(second.getSubject().getReference()).isEqualTo("Patient/" + PATIENT_UUID);
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    EncounterSearchQuery query = EncounterSearchQuery.builder().build();

    Page<FormEncounterDBRecord> page = new PageImpl<>(List.of());

    when(formEncounterRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    EncounterSearchQuery query = EncounterSearchQuery.builder().offset(10).count(5).build();

    Page<FormEncounterDBRecord> page = new PageImpl<>(List.of());

    when(formEncounterRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
  }
}

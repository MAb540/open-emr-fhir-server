package org.example.basicfhirserver.service.impl.medication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.MedicationMapper;
import org.example.basicfhirserver.mapper.impl.MedicationMapperImpl;
import org.example.basicfhirserver.query.resources.medication.MedicationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;
import org.example.basicfhirserver.repository.jdbc.drug.DrugRepository;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Medication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class MedicationServiceImplTest {

  @Mock private DrugRepository drugRepository;

  // The mapper is a pure function over a DB record - there is no I/O to mock, so the real
  // implementation is used and the assertions target the returned FHIR resource.
  private final MedicationMapper medicationMapper = new MedicationMapperImpl();

  private MedicationServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new MedicationServiceImpl(drugRepository, medicationMapper);
  }

  private static final UUID MEDICATION_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID SECOND_MEDICATION_UUID =
      UUID.fromString("987f6543-e21b-43c2-b456-426614174999");

  @Test
  void findById_returnsMappedMedication() {
    // Arrange: a real record with explicit mapping data
    DrugDBRecord record =
        DrugDBRecord.builder()
            .uuid(MEDICATION_UUID)
            .active(1)
            .form("2")
            .lotNumber("LOT-443322")
            .build();

    // Mock only the database boundary
    when(drugRepository.findById(MEDICATION_UUID)).thenReturn(List.of(record));

    // Act: trigger the public behavior contract
    Medication result = service.findById(MEDICATION_UUID);

    // Assert: validate the final observable behavior state
    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(MEDICATION_UUID.toString());
    assertThat(result.getStatus()).isEqualTo(Medication.MedicationStatus.ACTIVE);

    // Verify the mapped form
    assertThat(result.getForm().getCodingFirstRep().getDisplay()).isEqualTo("tablet");
    assertThat(result.getForm().getCodingFirstRep().getCode()).isEqualTo("C42998");

    // Verify the mapped batch details
    assertThat(result.getBatch().getLotNumber()).isEqualTo("LOT-443322");
  }

  @Test
  void findById_throwsNotFoundWhenNoRecords() {
    when(drugRepository.findById(MEDICATION_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(MEDICATION_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(MEDICATION_UUID.toString());
  }

  @Test
  void search_returnsMedicationsForEveryRecordOnThePage() {
    MedicationSearchQuery query = MedicationSearchQuery.builder().build();

    DrugDBRecord activeRecord =
        DrugDBRecord.builder().uuid(MEDICATION_UUID).active(1).form("2").build();
    DrugDBRecord inactiveRecord =
        DrugDBRecord.builder().uuid(SECOND_MEDICATION_UUID).active(0).form("1").build();
    Page<DrugDBRecord> page = new PageImpl<>(List.of(activeRecord, inactiveRecord));

    when(drugRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(2);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.currentOffset()).isZero();

    Medication first = (Medication) result.primaryResources().get(0);
    assertThat(first.getId()).isEqualTo(MEDICATION_UUID.toString());
    assertThat(first.getStatus()).isEqualTo(Medication.MedicationStatus.ACTIVE);
    assertThat(first.getForm().getCodingFirstRep().getCode()).isEqualTo("C42998");

    Medication second = (Medication) result.primaryResources().get(1);
    assertThat(second.getId()).isEqualTo(SECOND_MEDICATION_UUID.toString());
    assertThat(second.getStatus()).isEqualTo(Medication.MedicationStatus.INACTIVE);
    assertThat(second.getForm().getCodingFirstRep().getCode()).isEqualTo("C60928");
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    MedicationSearchQuery query = MedicationSearchQuery.builder().build();

    Page<DrugDBRecord> page = new PageImpl<>(List.of());

    when(drugRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    MedicationSearchQuery query = MedicationSearchQuery.builder().offset(10).count(5).build();

    Page<DrugDBRecord> page = new PageImpl<>(List.of());

    when(drugRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
  }
}

package org.example.basicfhirserver.service.impl.medication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.MedicationMapper;
import org.example.basicfhirserver.query.resources.medication.MedicationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.drug.DrugDBRecord;
import org.example.basicfhirserver.repository.jdbc.drug.DrugRepository;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Medication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")
class MedicationServiceImplTest {

  @Mock private DrugRepository drugRepository;
  @Mock private MedicationMapper medicationMapper;

  @InjectMocks private MedicationServiceImpl service;

  private static final UUID MEDICATION_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void findById_returnsMappedMedication() {
    DrugDBRecord record = mock(DrugDBRecord.class);
    Medication medication = new Medication();

    when(drugRepository.findById(MEDICATION_UUID)).thenReturn(List.of(record));
    when(medicationMapper.toR4(record)).thenReturn(medication);

    Medication result = service.findById(MEDICATION_UUID);

    assertThat(result).isSameAs(medication);
    verify(medicationMapper).toR4(record);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecords() {
    when(drugRepository.findById(MEDICATION_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(MEDICATION_UUID))
        .isInstanceOf(ResourceNotFoundException.class);

    verifyNoInteractions(medicationMapper);
  }

  @Test
  void search_returnsPrimaryMedications() {
    MedicationSearchQuery query = MedicationSearchQuery.builder().build();

    DrugDBRecord record = mock(DrugDBRecord.class);
    Page<DrugDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(record));
    when(page.getTotalElements()).thenReturn(1L);
    when(drugRepository.find(query)).thenReturn(page);

    Medication medication = new Medication();
    when(medicationMapper.toR4(record)).thenReturn(medication);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(medication);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.currentOffset()).isZero();
    verify(drugRepository).find(query);
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    MedicationSearchQuery query = MedicationSearchQuery.builder().build();

    Page<DrugDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
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

    Page<DrugDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
    when(drugRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
    verify(drugRepository).find(query);
  }
}

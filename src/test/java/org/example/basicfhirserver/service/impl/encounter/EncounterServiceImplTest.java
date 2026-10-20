package org.example.basicfhirserver.service.impl.encounter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.EncounterMapper;
import org.example.basicfhirserver.model.FormEncounter;
import org.example.basicfhirserver.query.resources.encounter.EncounterSearchQuery;
import org.example.basicfhirserver.repository.jdbc.formencounter.FormEncounterDBRecord;
import org.example.basicfhirserver.repository.jdbc.formencounter.FormEncounterRepository;
import org.example.basicfhirserver.service.assembler.formencounter.FormEncounterAssembler;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Encounter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class EncounterServiceImplTest {

  @Mock private FormEncounterRepository formEncounterRepository;
  @Mock private FormEncounterAssembler formEncounterAssembler;
  @Mock private EncounterMapper encounterMapper;

  @InjectMocks private EncounterServiceImpl service;

  private static final UUID ENCOUNTER_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void findById_returnsMappedEncounter() {
    FormEncounterDBRecord record = mock(FormEncounterDBRecord.class);
    FormEncounter formEncounter = new FormEncounter();
    Encounter encounter = new Encounter();

    when(formEncounterRepository.findById(ENCOUNTER_UUID)).thenReturn(List.of(record));
    when(formEncounterAssembler.toCanonical(record)).thenReturn(formEncounter);
    when(encounterMapper.toR4(formEncounter)).thenReturn(encounter);

    Encounter result = service.findById(ENCOUNTER_UUID);

    assertThat(result).isSameAs(encounter);
    verify(encounterMapper).toR4(formEncounter);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecords() {
    when(formEncounterRepository.findById(ENCOUNTER_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(ENCOUNTER_UUID))
        .isInstanceOf(ResourceNotFoundException.class);

    verifyNoInteractions(formEncounterAssembler, encounterMapper);
  }

  @Test
  void search_returnsPrimaryEncountersWithoutIncludes() {
    EncounterSearchQuery query = EncounterSearchQuery.builder().build();

    FormEncounterDBRecord record = mock(FormEncounterDBRecord.class);
    Page<FormEncounterDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(record));
    when(page.getTotalElements()).thenReturn(1L);
    when(formEncounterRepository.find(query)).thenReturn(page);

    FormEncounter formEncounter = new FormEncounter();
    when(formEncounterAssembler.toCanonical(record)).thenReturn(formEncounter);
    Encounter encounter = new Encounter();
    when(encounterMapper.toR4(formEncounter)).thenReturn(encounter);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(encounter);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.currentOffset()).isZero();
    verify(formEncounterRepository).find(query);
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    EncounterSearchQuery query = EncounterSearchQuery.builder().build();

    Page<FormEncounterDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
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

    Page<FormEncounterDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(page.getTotalElements()).thenReturn(0L);
    when(formEncounterRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
    verify(formEncounterRepository).find(query);
    verify(formEncounterAssembler, never()).toCanonical(any());
    verify(encounterMapper, never()).toR4(any());
  }
}

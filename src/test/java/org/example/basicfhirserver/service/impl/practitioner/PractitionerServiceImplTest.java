package org.example.basicfhirserver.service.impl.practitioner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.PractitionerMapper;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.example.basicfhirserver.repository.jdbc.user.UserRepository;
import org.example.basicfhirserver.service.assembler.users.UserAssembler;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

@ExtendWith(MockitoExtension.class)
class PractitionerServiceImplTest {

  @Mock private UserRepository userRepository;
  @Mock private UserAssembler userAssembler;
  @Mock private PractitionerMapper practitionerMapper;

  @InjectMocks private PractitionerServiceImpl service;

  private static final UUID PRACTITIONER_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void findById_returnsMappedPractitioner() {
    UserDBRecord record = mock(UserDBRecord.class);
    when(userRepository.findById(PRACTITIONER_UUID)).thenReturn(List.of(record));

    org.example.basicfhirserver.model.Practitioner canonical =
        new org.example.basicfhirserver.model.Practitioner();
    when(userAssembler.toCanonical(record)).thenReturn(canonical);

    Practitioner practitioner = new Practitioner();
    when(practitionerMapper.toR4(canonical)).thenReturn(practitioner);

    Practitioner result = service.findById(PRACTITIONER_UUID);

    assertThat(result).isSameAs(practitioner);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecord() {
    when(userRepository.findById(PRACTITIONER_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(PRACTITIONER_UUID))
        .isInstanceOf(ResourceNotFoundException.class);
    verify(practitionerMapper, never()).toR4(any());
  }

  @Test
  void search_returnsPrimaryPractitioners() {
    PractitionerSearchQuery query = PractitionerSearchQuery.builder().build();

    UserDBRecord record = mock(UserDBRecord.class);
    Page<UserDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of(record));
    when(page.getTotalElements()).thenReturn(1L);
    when(userRepository.find(query)).thenReturn(page);

    org.example.basicfhirserver.model.Practitioner canonical =
        new org.example.basicfhirserver.model.Practitioner();
    when(userAssembler.toCanonical(record)).thenReturn(canonical);

    Practitioner practitioner = new Practitioner();
    when(practitionerMapper.toR4(canonical)).thenReturn(practitioner);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).containsExactly(practitioner);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    PractitionerSearchQuery query = PractitionerSearchQuery.builder().count(10).offset(20).build();

    Page<UserDBRecord> page = mock(Page.class);
    when(page.getContent()).thenReturn(List.of());
    when(userRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(20);
    assertThat(result.primaryResources()).isEmpty();
  }
}

package org.example.basicfhirserver.service.impl.practitioner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.PractitionerMapper;
import org.example.basicfhirserver.mapper.impl.PractitionerMapperImpl;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.query.resources.practitioner.PractitionerSearchQuery;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.example.basicfhirserver.repository.jdbc.user.UserRepository;
import org.example.basicfhirserver.service.assembler.users.UserAssembler;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class PractitionerServiceImplTest {

  @Mock private UserRepository userRepository;

  // The assembler and the mapper are pure functions over a DB record - there is no I/O to mock,
  // so the real implementations are used and the assertions target the returned FHIR resource.
  private final UserAssembler userAssembler = new UserAssembler();
  private final PractitionerMapper practitionerMapper = new PractitionerMapperImpl();

  private PractitionerServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new PractitionerServiceImpl(userRepository, userAssembler, practitionerMapper);
  }

  private static final UUID PRACTITIONER_UUID =
      UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID SECOND_PRACTITIONER_UUID =
      UUID.fromString("987f6543-e21b-43c2-b456-426614174999");
  private static final Instant LAST_UPDATED = Instant.parse("2026-01-16T08:00:00Z");

  private static UserDBRecord record() {
    return UserDBRecord.builder()
        .id(1L)
        .uuid(PRACTITIONER_UUID)
        .fname("John")
        .mname("Quincy")
        .lname("Doe")
        .active(1)
        .npi("1234567890")
        .email("john.doe@example.com")
        .phone("555-0100")
        .street("1 Main Street")
        .city("Boston")
        .state("MA")
        .zip("02108")
        .countryCode("US")
        .lastUpdated(LAST_UPDATED)
        .build();
  }

  private static UserDBRecord secondRecord() {
    return UserDBRecord.builder()
        .id(2L)
        .uuid(SECOND_PRACTITIONER_UUID)
        .fname("Jane")
        .lname("Roe")
        .active(0)
        .lastUpdated(LAST_UPDATED)
        .build();
  }

  @Test
  void findById_returnsMappedPractitioner() {
    when(userRepository.findById(PRACTITIONER_UUID)).thenReturn(List.of(record()));

    Practitioner result = service.findById(PRACTITIONER_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(PRACTITIONER_UUID.toString());
    assertThat(result.getActive()).isTrue();
    assertThat(result.getName().get(0).getFamily()).isEqualTo("Doe");
    assertThat(result.getName().get(0).getGiven())
        .extracting(given -> given.getValue())
        .containsExactly("John", "Quincy");
    assertThat(result.getIdentifier().get(0).getSystem())
        .isEqualTo("http://hl7.org/fhir/sid/us-npi");
    assertThat(result.getIdentifier().get(0).getValue()).isEqualTo("1234567890");
    assertThat(result.getTelecom().get(0).getSystem())
        .isEqualTo(ContactPoint.ContactPointSystem.PHONE);
    assertThat(result.getTelecom().get(0).getValue()).isEqualTo("555-0100");
    assertThat(result.getTelecom().get(1).getSystem())
        .isEqualTo(ContactPoint.ContactPointSystem.EMAIL);
    assertThat(result.getTelecom().get(1).getValue()).isEqualTo("john.doe@example.com");
    assertThat(result.getAddress().get(0).getLine().get(0).getValue()).isEqualTo("1 Main Street");
    assertThat(result.getAddress().get(0).getCity()).isEqualTo("Boston");
    assertThat(result.getAddress().get(0).getState()).isEqualTo("MA");
    assertThat(result.getAddress().get(0).getPostalCode()).isEqualTo("02108");
    assertThat(result.getAddress().get(0).getCountry()).isEqualTo("US");
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(ProfilesConstants.HL7_US_CORE_PRACTITIONER);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecords() {
    when(userRepository.findById(PRACTITIONER_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(PRACTITIONER_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(PRACTITIONER_UUID.toString());
  }

  @Test
  void search_returnsPractitionersForEveryRecordOnThePage() {
    PractitionerSearchQuery query = PractitionerSearchQuery.builder().build();

    Page<UserDBRecord> page = new PageImpl<>(List.of(record(), secondRecord()));

    when(userRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(2);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.currentOffset()).isZero();

    Practitioner first = (Practitioner) result.primaryResources().get(0);
    assertThat(first.getId()).isEqualTo(PRACTITIONER_UUID.toString());
    assertThat(first.getName().get(0).getFamily()).isEqualTo("Doe");
    assertThat(first.getActive()).isTrue();

    Practitioner second = (Practitioner) result.primaryResources().get(1);
    assertThat(second.getId()).isEqualTo(SECOND_PRACTITIONER_UUID.toString());
    assertThat(second.getName().get(0).getFamily()).isEqualTo("Roe");
    assertThat(second.getActive()).isFalse();
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    PractitionerSearchQuery query = PractitionerSearchQuery.builder().build();

    Page<UserDBRecord> page = new PageImpl<>(List.of());

    when(userRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    PractitionerSearchQuery query = PractitionerSearchQuery.builder().offset(10).count(5).build();

    Page<UserDBRecord> page = new PageImpl<>(List.of());

    when(userRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
  }
}

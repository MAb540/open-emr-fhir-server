package org.example.basicfhirserver.service.impl.person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.PersonMapper;
import org.example.basicfhirserver.mapper.impl.PersonMapperImpl;
import org.example.basicfhirserver.query.resources.person.PersonSearchQuery;
import org.example.basicfhirserver.repository.jdbc.user.UserDBRecord;
import org.example.basicfhirserver.repository.jdbc.user.UserRepository;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class PersonServiceImplTest {

  @Mock private UserRepository userRepository;

  // The mapper is a pure function over a DB record - there is no I/O to mock, so the real
  // implementation is used and the assertions target the returned FHIR resource.
  private final PersonMapper personMapper = new PersonMapperImpl();

  private PersonServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new PersonServiceImpl(userRepository, personMapper);
  }

  private static final UUID USER_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID SECOND_USER_UUID =
      UUID.fromString("223e4567-e89b-12d3-a456-426614174001");

  private static UserDBRecord record(UUID uuid, String firstName, String lastName) {
    return UserDBRecord.builder()
        .id(1L)
        .uuid(uuid)
        .username("jdoe")
        .active(1)
        .title("Dr")
        .fname(firstName)
        .lname(lastName)
        .phone("617-555-0100")
        .email("jdoe@example.org")
        .street("1 Main Street")
        .city("Boston")
        .state("MA")
        .zip("02110")
        .build();
  }

  @Test
  void findById_returnsMappedPerson() {
    when(userRepository.findById(USER_UUID)).thenReturn(List.of(record(USER_UUID, "Jane", "Doe")));

    Person result = service.findById(USER_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(USER_UUID.toString());
    assertThat(result.getActive()).isTrue();
    assertThat(result.getName().get(0).getFamily()).isEqualTo("Doe");
    assertThat(result.getName().get(0).getGiven().get(0).getValue()).isEqualTo("Jane");
    assertThat(result.getName().get(0).getPrefix().get(0).getValue()).isEqualTo("Dr");
    assertThat(result.getTelecom().get(0).getValue()).isEqualTo("617-555-0100");
    assertThat(result.getAddress().get(0).getLine().get(0).getValue()).isEqualTo("1 Main Street");
    assertThat(result.getAddress().get(0).getCity()).isEqualTo("Boston");
    assertThat(result.getAddress().get(0).getPostalCode()).isEqualTo("02110");
  }

  @Test
  void findById_throwsNotFoundWhenNoRecords() {
    when(userRepository.findById(USER_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(USER_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(USER_UUID.toString());
  }

  @Test
  void search_returnsPersonsForEveryRecordOnThePage() {
    PersonSearchQuery query = PersonSearchQuery.builder().build();

    Page<UserDBRecord> page =
        new PageImpl<>(
            List.of(record(USER_UUID, "Jane", "Doe"), record(SECOND_USER_UUID, "John", "Roe")));

    when(userRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(2);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.currentOffset()).isZero();

    Person first = (Person) result.primaryResources().get(0);
    assertThat(first.getId()).isEqualTo(USER_UUID.toString());
    assertThat(first.getName().get(0).getFamily()).isEqualTo("Doe");

    Person second = (Person) result.primaryResources().get(1);
    assertThat(second.getId()).isEqualTo(SECOND_USER_UUID.toString());
    assertThat(second.getName().get(0).getFamily()).isEqualTo("Roe");
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    PersonSearchQuery query = PersonSearchQuery.builder().build();

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
    PersonSearchQuery query = PersonSearchQuery.builder().offset(10).count(5).build();

    Page<UserDBRecord> page = new PageImpl<>(List.of());

    when(userRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
  }
}

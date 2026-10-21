package org.example.basicfhirserver.service.impl.organization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.OrganizationMapper;
import org.example.basicfhirserver.mapper.impl.OrganizationMapperImpl;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.query.resources.organization.OrganizationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.facility.FacilityDBRecord;
import org.example.basicfhirserver.repository.jdbc.facility.FacilityRepository;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

  @Mock private FacilityRepository facilityRepository;

  // The mapper is a pure function over a DB record - there is no I/O to mock, so the real
  // implementation is used and the assertions target the returned FHIR resource.
  private final OrganizationMapper organizationMapper = new OrganizationMapperImpl();

  private OrganizationServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new OrganizationServiceImpl(facilityRepository, organizationMapper);
  }

  private static final UUID FACILITY_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID SECOND_FACILITY_UUID =
      UUID.fromString("223e4567-e89b-12d3-a456-426614174001");

  private static FacilityDBRecord record(UUID uuid, String name) {
    return FacilityDBRecord.builder()
        .id(1L)
        .uuid(uuid)
        .name(name)
        .inactive(0)
        .facilityNpi("1234567890")
        .phone("617-555-0100")
        .email("info@example.org")
        .street("1 Main Street")
        .city("Boston")
        .state("MA")
        .postalCode("02110")
        .countryCode("US")
        .build();
  }

  @Test
  void findById_returnsMappedOrganization() {
    when(facilityRepository.findById(FACILITY_UUID))
        .thenReturn(List.of(record(FACILITY_UUID, "General Hospital")));

    Organization result = service.findById(FACILITY_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(FACILITY_UUID.toString());
    assertThat(result.getActive()).isTrue();
    assertThat(result.getName()).isEqualTo("General Hospital");
    assertThat(result.getIdentifier().get(0).getValue()).isEqualTo("1234567890");
    assertThat(result.getIdentifier().get(0).getSystem())
        .isEqualTo("http://hl7.org/fhir/sid/us-npi");
    assertThat(result.getTelecom().get(0).getValue()).isEqualTo("617-555-0100");
    assertThat(result.getAddress().get(0).getLine().get(0).getValue()).isEqualTo("1 Main Street");
    assertThat(result.getAddress().get(0).getCity()).isEqualTo("Boston");
    assertThat(result.getAddress().get(0).getState()).isEqualTo("MA");
    assertThat(result.getAddress().get(0).getPostalCode()).isEqualTo("02110");
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(ProfilesConstants.HL7_US_CORE_ORGANIZATION);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecords() {
    when(facilityRepository.findById(FACILITY_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(FACILITY_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(FACILITY_UUID.toString());
  }

  @Test
  void search_returnsOrganizationsForEveryRecordOnThePage() {
    OrganizationSearchQuery query = OrganizationSearchQuery.builder().build();

    Page<FacilityDBRecord> page =
        new PageImpl<>(
            List.of(
                record(FACILITY_UUID, "General Hospital"),
                record(SECOND_FACILITY_UUID, "Community Clinic")));

    when(facilityRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(2);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.currentOffset()).isZero();

    Organization first = (Organization) result.primaryResources().get(0);
    assertThat(first.getId()).isEqualTo(FACILITY_UUID.toString());
    assertThat(first.getName()).isEqualTo("General Hospital");

    Organization second = (Organization) result.primaryResources().get(1);
    assertThat(second.getId()).isEqualTo(SECOND_FACILITY_UUID.toString());
    assertThat(second.getName()).isEqualTo("Community Clinic");
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    OrganizationSearchQuery query = OrganizationSearchQuery.builder().build();

    Page<FacilityDBRecord> page = new PageImpl<>(List.of());

    when(facilityRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    OrganizationSearchQuery query = OrganizationSearchQuery.builder().offset(10).count(5).build();

    Page<FacilityDBRecord> page = new PageImpl<>(List.of());

    when(facilityRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
  }
}

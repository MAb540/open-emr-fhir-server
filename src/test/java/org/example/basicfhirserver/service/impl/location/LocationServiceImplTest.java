package org.example.basicfhirserver.service.impl.location;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.LocationMapper;
import org.example.basicfhirserver.mapper.impl.LocationMapperImpl;
import org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.query.resources.location.LocationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.location.LocationDBRecord;
import org.example.basicfhirserver.repository.jdbc.location.LocationRepository;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

  @Mock private LocationRepository locationRepository;

  // The mapper is a pure function over a DB record - there is no I/O to mock, so the real
  // implementation is used and the assertions target the returned FHIR resource.
  private final LocationMapper locationMapper = new LocationMapperImpl();

  private LocationServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new LocationServiceImpl(locationRepository, locationMapper);
  }

  private static final UUID LOCATION_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID SECOND_LOCATION_UUID =
      UUID.fromString("223e4567-e89b-12d3-a456-426614174001");

  private static LocationDBRecord record(UUID fhirLocationUuid, String name) {
    return LocationDBRecord.builder()
        .tableUuid(UUID.fromString("323e4567-e89b-12d3-a456-426614174002"))
        .name(name)
        .identifier("1234567890")
        .identifierType("npi")
        .street("1 Main Street")
        .city("Boston")
        .state("MA")
        .postalCode("02110")
        .countryCode("US")
        .phone("617-555-0100")
        .fax("617-555-0199")
        .email("info@example.org")
        .website("https://example.org")
        .lastUpdated(LocalDateTime.of(2026, 1, 16, 8, 0))
        .type("FACILITY")
        .locationRoleType("11")
        .fhirLocationUuid(fhirLocationUuid)
        .build();
  }

  @Test
  void findById_returnsMappedLocation() {
    when(locationRepository.findById(LOCATION_UUID))
        .thenReturn(List.of(record(LOCATION_UUID, "General Hospital")));

    Location result = service.findById(LOCATION_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(LOCATION_UUID.toString());
    assertThat(result.getName()).isEqualTo("General Hospital");
    assertThat(result.getStatus()).isEqualTo(Location.LocationStatus.ACTIVE);
    assertThat(result.getIdentifierFirstRep().getValue()).isEqualTo("1234567890");
    assertThat(result.getIdentifierFirstRep().getSystem())
        .isEqualTo(FhirCodeSystemConstants.PROVIDER_NPI);
    assertThat(result.getTypeFirstRep().getCodingFirstRep().getCode()).isEqualTo("11");
    assertThat(result.getAddress().getLine().get(0).getValue()).isEqualTo("1 Main Street");
    assertThat(result.getAddress().getCity()).isEqualTo("Boston");
    assertThat(result.getAddress().getState()).isEqualTo("MA");
    assertThat(result.getAddress().getPostalCode()).isEqualTo("02110");
    assertThat(result.getAddress().getCountry()).isEqualTo("US");
    assertThat(result.getTelecom().get(0).getSystem())
        .isEqualTo(ContactPoint.ContactPointSystem.PHONE);
    assertThat(result.getTelecom().get(0).getValue()).isEqualTo("617-555-0100");
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(ProfilesConstants.HL7_US_CORE_LOCATION);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecord() {
    when(locationRepository.findById(LOCATION_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(LOCATION_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(LOCATION_UUID.toString());
  }

  @Test
  void search_returnsLocationsForEveryRecordOnThePage() {
    LocationSearchQuery query = LocationSearchQuery.builder().build();

    Page<LocationDBRecord> page =
        new PageImpl<>(
            List.of(
                record(LOCATION_UUID, "General Hospital"),
                record(SECOND_LOCATION_UUID, "Community Clinic")));

    when(locationRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(2);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.currentOffset()).isZero();

    Location first = (Location) result.primaryResources().get(0);
    assertThat(first.getId()).isEqualTo(LOCATION_UUID.toString());
    assertThat(first.getName()).isEqualTo("General Hospital");

    Location second = (Location) result.primaryResources().get(1);
    assertThat(second.getId()).isEqualTo(SECOND_LOCATION_UUID.toString());
    assertThat(second.getName()).isEqualTo("Community Clinic");
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    LocationSearchQuery query = LocationSearchQuery.builder().build();

    when(locationRepository.find(query)).thenReturn(new PageImpl<>(List.of()));

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    LocationSearchQuery query = LocationSearchQuery.builder().offset(10).count(5).build();

    when(locationRepository.find(query)).thenReturn(new PageImpl<>(List.of()));

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
  }
}

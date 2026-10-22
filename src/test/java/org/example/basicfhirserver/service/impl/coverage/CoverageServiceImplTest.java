package org.example.basicfhirserver.service.impl.coverage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.mapper.CoverageMapper;
import org.example.basicfhirserver.mapper.impl.CoverageMapperImpl;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.query.resources.coverage.CoverageSearchQuery;
import org.example.basicfhirserver.repository.jdbc.insurance.CoverageDBRecord;
import org.example.basicfhirserver.repository.jdbc.insurance.CoverageRepository;
import org.example.basicfhirserver.service.impl.SearchResult;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class CoverageServiceImplTest {

  @Mock private CoverageRepository coverageRepository;

  // The mapper is a pure function over a DB record - there is no I/O to mock, so the real
  // implementation is used and the assertions target the returned FHIR resource.
  private final CoverageMapper coverageMapper = new CoverageMapperImpl();

  private CoverageServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new CoverageServiceImpl(coverageRepository, coverageMapper);
  }

  private static final UUID COVERAGE_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID SECOND_COVERAGE_UUID =
      UUID.fromString("223e4567-e89b-12d3-a456-426614174001");
  private static final UUID PATIENT_UUID = UUID.fromString("323e4567-e89b-12d3-a456-426614174002");
  private static final UUID INSURER_UUID = UUID.fromString("423e4567-e89b-12d3-a456-426614174003");
  private static final LocalDateTime START_DATE = LocalDateTime.of(2026, 1, 1, 0, 0);
  private static final LocalDateTime END_DATE = LocalDateTime.of(2026, 12, 31, 0, 0);

  private static CoverageDBRecord record(
      UUID uuid, String policyNumber, LocalDateTime date, LocalDateTime dateEnd) {
    return CoverageDBRecord.builder()
        .id(1L)
        .uuid(uuid)
        .puuid(PATIENT_UUID)
        .insureruuid(INSURER_UUID)
        .type("primary")
        .planName("Blue Cross PPO")
        .policyNumber(policyNumber)
        .policyType("PPO")
        .groupNumber("GRP-42")
        .subscriberRelationship("child")
        .date(date)
        .dateEnd(dateEnd)
        .insTypeCode(3)
        .copay("25.00")
        .build();
  }

  @Test
  void findById_returnsMappedCoverage() {
    when(coverageRepository.findById(COVERAGE_UUID))
        .thenReturn(List.of(record(COVERAGE_UUID, "POL-12345", START_DATE, END_DATE)));

    Coverage result = service.findById(COVERAGE_UUID);

    assertThat(result).isNotNull();
    assertThat(result.getId()).isEqualTo(COVERAGE_UUID.toString());
    assertThat(result.getStatus()).isEqualTo(Coverage.CoverageStatus.ACTIVE);
    assertThat(result.getType().getCodingFirstRep().getCode()).isEqualTo("HIP");
    assertThat(result.getType().getCodingFirstRep().getDisplay())
        .isEqualTo("health insurance plan number");
    assertThat(result.getSubscriberId()).isEqualTo("POL-12345");
    assertThat(result.getBeneficiary().getReference()).isEqualTo("Patient/" + PATIENT_UUID);
    assertThat(result.getPayor().get(0).getReference()).isEqualTo("Organization/" + INSURER_UUID);
    assertThat(result.getRelationship().getCodingFirstRep().getCode()).isEqualTo("child");
    assertThat(result.getDependent()).isEqualTo("1");
    assertThat(result.getOrder()).isEqualTo(1);
    assertThat(result.getPeriod().getStart())
        .isEqualTo(Date.from(START_DATE.atZone(ZoneId.systemDefault()).toInstant()));
    assertThat(result.getPeriod().getEnd())
        .isEqualTo(Date.from(END_DATE.atZone(ZoneId.systemDefault()).toInstant()));
    assertThat(result.getIdentifier().get(0).getValue()).isEqualTo("POL-12345");
    assertThat(result.getIdentifier().get(0).getType().getCodingFirstRep().getCode())
        .isEqualTo("MB");
    assertThat(result.getIdentifier().get(1).getValue()).isEqualTo("PPO");
    assertThat(result.getIdentifier().get(1).getSystem())
        .isEqualTo("http://openemr.org/fhir/policy-type");
    assertThat(result.getClass_().get(0).getValue()).isEqualTo("GRP-42");
    assertThat(result.getClass_().get(0).getType().getCodingFirstRep().getCode())
        .isEqualTo("group");
    assertThat(result.getClass_().get(1).getValue()).isEqualTo("Blue Cross PPO");
    assertThat(result.getClass_().get(1).getType().getCodingFirstRep().getCode()).isEqualTo("plan");
    Money copay = (Money) result.getCostToBeneficiary().get(0).getValue();
    assertThat(copay.getValue().intValue()).isEqualTo(25);
    assertThat(copay.getCurrency()).isEqualTo("USD");
    assertThat(result.getMeta().getProfile().stream().map(CanonicalType::getValue))
        .contains(ProfilesConstants.HL7_US_CORE_COVERAGE);
  }

  @Test
  void findById_throwsNotFoundWhenNoRecords() {
    when(coverageRepository.findById(COVERAGE_UUID)).thenReturn(List.of());

    assertThatThrownBy(() -> service.findById(COVERAGE_UUID))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining(COVERAGE_UUID.toString());
  }

  @Test
  void search_returnsCoveragesWithTheirOwnStatus() {
    CoverageSearchQuery query = CoverageSearchQuery.builder().build();

    Page<CoverageDBRecord> page =
        new PageImpl<>(
            List.of(
                record(COVERAGE_UUID, "POL-12345", START_DATE, END_DATE),
                record(
                    SECOND_COVERAGE_UUID,
                    "POL-67890",
                    LocalDateTime.of(2025, 1, 1, 0, 0),
                    LocalDateTime.of(2025, 6, 30, 0, 0))));

    when(coverageRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).hasSize(2);
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(2);
    assertThat(result.currentOffset()).isZero();

    Coverage active = (Coverage) result.primaryResources().get(0);
    assertThat(active.getId()).isEqualTo(COVERAGE_UUID.toString());
    assertThat(active.getStatus()).isEqualTo(Coverage.CoverageStatus.ACTIVE);
    assertThat(active.getSubscriberId()).isEqualTo("POL-12345");

    Coverage cancelled = (Coverage) result.primaryResources().get(1);
    assertThat(cancelled.getId()).isEqualTo(SECOND_COVERAGE_UUID.toString());
    assertThat(cancelled.getStatus()).isEqualTo(Coverage.CoverageStatus.CANCELLED);
    assertThat(cancelled.getSubscriberId()).isEqualTo("POL-67890");
  }

  @Test
  void search_returnsEmptyResultWhenNoRecords() {
    CoverageSearchQuery query = CoverageSearchQuery.builder().build();

    Page<CoverageDBRecord> page = new PageImpl<>(List.of());

    when(coverageRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.primaryResources()).isEmpty();
    assertThat(result.includedResources()).isEmpty();
    assertThat(result.totalElements()).isZero();
    assertThat(result.currentOffset()).isZero();
  }

  @Test
  void search_forwardsOffsetWhenProvided() {
    CoverageSearchQuery query = CoverageSearchQuery.builder().offset(10).count(5).build();

    Page<CoverageDBRecord> page = new PageImpl<>(List.of());

    when(coverageRepository.find(query)).thenReturn(page);

    SearchResult result = service.search(query);

    assertThat(result.currentOffset()).isEqualTo(10);
  }
}

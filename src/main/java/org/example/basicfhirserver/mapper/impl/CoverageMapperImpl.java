package org.example.basicfhirserver.mapper.impl;

import static org.example.basicfhirserver.mapper.utils.MapperHelper.getUnknownCodeableConcept;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import org.example.basicfhirserver.mapper.CoverageMapper;
import org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.repository.jdbc.insurance.CoverageDBRecord;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class CoverageMapperImpl implements CoverageMapper {

  private static final String DEFAULT_COVERAGE_TYPE = "HIP";
  private static final String DEFAULT_COVERAGE_TYPE_DISPLAY = "health insurance plan number";
  private static final BigDecimal MINIMUM_COPAY = new BigDecimal("0.01");

  private static final Map<Integer, String> INS_TYPE_CODE_MAP =
      Map.ofEntries(
          Map.entry(1, "PUBLICPOL"),
          Map.entry(2, "PUBLICPOL"),
          Map.entry(3, "HIP"),
          Map.entry(4, "HIP"),
          Map.entry(5, "PUBLICPOL"),
          Map.entry(6, "PUBLICPOL"),
          Map.entry(7, "HIP"),
          Map.entry(8, "HIP"),
          Map.entry(9, "PUBLICPOL"),
          Map.entry(10, "HIP"),
          Map.entry(11, "HIP"),
          Map.entry(12, "HIP"),
          Map.entry(13, "HIP"),
          Map.entry(14, "HIP"),
          Map.entry(15, "HIP"),
          Map.entry(16, "HIP"),
          Map.entry(17, "HIP"));

  private static final Map<String, String> COVERAGE_TYPE_DISPLAY =
      Map.of(
          "HIP", "health insurance plan number",
          "PUBLICPOL", "public healthcare",
          "EHCPOL", "extended healthcare");

  @Override
  public Coverage toR4(CoverageDBRecord coverageDBRecord) {
    Coverage coverage = new Coverage();

    coverage.setMeta(populateMeta(coverageDBRecord));

    coverage.setId(
        coverageDBRecord.getUuid() != null
            ? coverageDBRecord.getUuid().toString()
            : coverageDBRecord.getId().toString());

    Identifier policyNumberIdentifier = populatePolicyNumberIdentifier(coverageDBRecord);
    if (policyNumberIdentifier != null) {
      coverage.addIdentifier(policyNumberIdentifier);
    }

    Identifier policyTypeIdentifier = populatePolicyTypeIdentifier(coverageDBRecord);
    if (policyTypeIdentifier != null) {
      coverage.addIdentifier(policyTypeIdentifier);
    }

    coverage.setStatus(populateStatus(coverageDBRecord));
    coverage.setType(populateType(coverageDBRecord));

    if (coverageDBRecord.getPolicyNumber() != null
        && !coverageDBRecord.getPolicyNumber().isEmpty()) {
      coverage.setSubscriberId(coverageDBRecord.getPolicyNumber());
    }

    coverage.setBeneficiary(populatePatientReference(coverageDBRecord.getPuuid()));

    String subscriberRelationship = coverageDBRecord.getSubscriberRelationship();
    if (subscriberRelationship != null && !"self".equalsIgnoreCase(subscriberRelationship)) {
      coverage.setDependent("1");
    }

    CodeableConcept relationship = populateRelationship(coverageDBRecord);
    if (relationship != null) {
      coverage.setRelationship(relationship);
    }

    Period period = populatePeriod(coverageDBRecord);
    if (period != null) {
      coverage.setPeriod(period);
    }

    coverage.addPayor(populateOrganizationReference(coverageDBRecord.getInsureruuid()));

    populateClass(coverage, coverageDBRecord);
    populateOrder(coverage, coverageDBRecord);
    populateCostToBeneficiary(coverage, coverageDBRecord);

    return coverage;
  }

  private Meta populateMeta(CoverageDBRecord coverageDBRecord) {
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.addProfile(ProfilesConstants.HL7_US_CORE_COVERAGE);
    meta.setLastUpdated(populateLastUpdated(coverageDBRecord));
    return meta;
  }

  private Date populateLastUpdated(CoverageDBRecord coverageDBRecord) {
    if (coverageDBRecord.getDate() != null) {
      return toDate(coverageDBRecord.getDate());
    }
    return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
  }

  private Coverage.CoverageStatus populateStatus(CoverageDBRecord coverageDBRecord) {
    LocalDate now = LocalDate.now();

    LocalDateTime startDateTime = coverageDBRecord.getDate();
    LocalDateTime endDateTime = coverageDBRecord.getDateEnd();

    if (startDateTime == null) {
      return Coverage.CoverageStatus.ACTIVE;
    }

    LocalDate startDate = startDateTime.toLocalDate();
    LocalDate endDate = endDateTime != null ? endDateTime.toLocalDate() : null;

    if (startDate.isAfter(now)) {
      return Coverage.CoverageStatus.DRAFT;
    }

    if (endDate != null && endDate.isBefore(now)) {
      return Coverage.CoverageStatus.CANCELLED;
    }

    if ((startDate.isBefore(now) || startDate.isEqual(now))
        && (endDate == null || endDate.isAfter(now) || endDate.isEqual(now))) {
      return Coverage.CoverageStatus.ACTIVE;
    }

    return Coverage.CoverageStatus.ACTIVE;
  }

  private CodeableConcept populateType(CoverageDBRecord coverageDBRecord) {
    String code = DEFAULT_COVERAGE_TYPE;
    Integer insTypeCode = coverageDBRecord.getInsTypeCode();
    if (insTypeCode != null && INS_TYPE_CODE_MAP.containsKey(insTypeCode)) {
      code = INS_TYPE_CODE_MAP.get(insTypeCode);
    }

    Coding coding = new Coding();
    coding.setSystem(FhirCodeSystemConstants.HL7_V3_ACT_CODE);
    coding.setCode(code);
    coding.setDisplay(COVERAGE_TYPE_DISPLAY.getOrDefault(code, DEFAULT_COVERAGE_TYPE_DISPLAY));
    return new CodeableConcept().addCoding(coding);
  }

  private Identifier populatePolicyNumberIdentifier(CoverageDBRecord coverageDBRecord) {
    String policyNumber = coverageDBRecord.getPolicyNumber();
    if (policyNumber == null || policyNumber.isEmpty()) {
      return null;
    }
    Identifier identifier = new Identifier();

    identifier.setType(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/v2-0203")
                    .setCode("MB")
                    .setDisplay("Member Number")));
    identifier.setValue(policyNumber);
    return identifier;
  }

  private Identifier populatePolicyTypeIdentifier(CoverageDBRecord coverageDBRecord) {
    String policyType = coverageDBRecord.getPolicyType();
    if (policyType == null || policyType.isEmpty()) {
      return null;
    }
    Identifier identifier = new Identifier();
    identifier.setSystem("http://openemr.org/fhir/policy-type");
    identifier.setValue(policyType);
    return identifier;
  }

  private Reference populatePatientReference(UUID patientUuid) {
    Reference reference = new Reference();
    if (patientUuid != null) {
      reference.setReference("Patient/" + patientUuid);
    } else {
      reference.addExtension(populateDataAbsentExtension());
    }
    return reference;
  }

  private Reference populateOrganizationReference(UUID organizationUuid) {
    Reference reference = new Reference();
    if (organizationUuid != null) {
      reference.setReference("Organization/" + organizationUuid);
    } else {
      reference.addExtension(populateDataAbsentExtension());
    }
    return reference;
  }

  private Extension populateDataAbsentExtension() {
    Extension dataMissingExtension =
        new Extension(FhirCodeSystemConstants.DATA_ABSENT_REASON_EXTENSION);
    dataMissingExtension.setValue(getUnknownCodeableConcept());
    return dataMissingExtension;
  }

  private CodeableConcept populateRelationship(CoverageDBRecord coverageDBRecord) {
    String relationship = coverageDBRecord.getSubscriberRelationship();
    if (relationship == null || relationship.isEmpty()) {
      return null;
    }
    Coding coding = new Coding();
    coding.setSystem(FhirCodeSystemConstants.SUBSCRIBER_RELATIONSHIP);
    coding.setCode(relationship);
    coding.setDisplay(relationship);
    return new CodeableConcept().addCoding(coding);
  }

  private Period populatePeriod(CoverageDBRecord coverageDBRecord) {
    if (coverageDBRecord.getDate() == null && coverageDBRecord.getDateEnd() == null) {
      return null;
    }
    Period period = new Period();
    if (coverageDBRecord.getDate() != null) {
      period.setStart(toDate(coverageDBRecord.getDate()));
    }
    if (coverageDBRecord.getDateEnd() != null) {
      period.setEnd(toDate(coverageDBRecord.getDateEnd()));
    }
    return period;
  }

  private void populateClass(Coverage coverage, CoverageDBRecord coverageDBRecord) {
    String groupNumber = coverageDBRecord.getGroupNumber();
    if (groupNumber != null && !groupNumber.isEmpty()) {
      Coverage.ClassComponent groupClass = new Coverage.ClassComponent();
      groupClass.setType(populateClassType("group", "Group"));
      groupClass.setValue(groupNumber);
      if (coverageDBRecord.getPlanName() != null) {
        groupClass.setName(coverageDBRecord.getPlanName());
      }
      coverage.addClass_(groupClass);
    }

    String planName = coverageDBRecord.getPlanName();
    if (planName != null && !planName.isEmpty()) {
      Coverage.ClassComponent planClass = new Coverage.ClassComponent();
      planClass.setType(populateClassType("plan", "Plan"));
      planClass.setValue(planName);
      planClass.setName(planName);
      coverage.addClass_(planClass);
    }
  }

  private CodeableConcept populateClassType(String code, String display) {
    Coding coding = new Coding();
    coding.setSystem(FhirCodeSystemConstants.COVERAGE_CLASS);
    coding.setCode(code);
    coding.setDisplay(display);
    return new CodeableConcept().addCoding(coding);
  }

  private void populateOrder(Coverage coverage, CoverageDBRecord coverageDBRecord) {
    Integer order = mapCoverageOrder(coverageDBRecord.getType());
    if (order != null) {
      coverage.setOrder(order);
    }
  }

  private Integer mapCoverageOrder(String insuranceType) {
    if (insuranceType == null) {
      return null;
    }
    return switch (insuranceType) {
      case "primary" -> 1;
      case "secondary" -> 2;
      case "tertiary" -> 3;
      default -> null;
    };
  }

  private void populateCostToBeneficiary(Coverage coverage, CoverageDBRecord coverageDBRecord) {
    BigDecimal copay = coverageDBRecord.getCopay();
    if (copay == null || copay.compareTo(MINIMUM_COPAY) < 0) {
      return;
    }

    Coverage.CostToBeneficiaryComponent costToBeneficiary =
        new Coverage.CostToBeneficiaryComponent();

    Coding coding = new Coding();
    coding.setSystem(FhirCodeSystemConstants.COVERAGE_COPAY_TYPE);
    coding.setCode("copay");
    coding.setDisplay("Copay");
    costToBeneficiary.setType(new CodeableConcept().addCoding(coding));

    Money value = new Money();
    value.setValue(copay);
    value.setCurrency("USD");
    costToBeneficiary.setValue(value);

    coverage.addCostToBeneficiary(costToBeneficiary);
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}

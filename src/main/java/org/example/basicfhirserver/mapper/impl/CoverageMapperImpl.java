package org.example.basicfhirserver.mapper.impl;

import static org.example.basicfhirserver.mapper.utils.MapperHelper.getUnknownCodeableConcept;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import org.example.basicfhirserver.mapper.CoverageMapper;
import org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.repository.jdbc.insurance.CoverageDBRecord;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class CoverageMapperImpl implements CoverageMapper {

  @Override
  public Coverage toR4(CoverageDBRecord coverageDBRecord) {
    Coverage coverage = new Coverage();

    coverage.getMeta().addProfile(ProfilesConstants.HL7_US_CORE_COVERAGE);
    coverage.getMeta().setVersionId("1");

    coverage.setId(
        coverageDBRecord.getUuid() != null
            ? coverageDBRecord.getUuid().toString()
            : coverageDBRecord.getId().toString());

    coverage.setStatus(populateStatus(coverageDBRecord));

    CodeableConcept type = populateType(coverageDBRecord);
    if (type != null) {
      coverage.setType(type);
    }

    Identifier identifier = populateIdentifier(coverageDBRecord);
    if (identifier != null) {
      coverage.addIdentifier(identifier);
    }

    coverage.setSubscriber(populatePatientReference(coverageDBRecord.getPuuid()));
    coverage.setBeneficiary(populatePatientReference(coverageDBRecord.getPuuid()));

    CodeableConcept relationship = populateRelationship(coverageDBRecord);
    if (relationship != null) {
      coverage.setRelationship(relationship);
    }

    coverage.setPeriod(populatePeriod(coverageDBRecord));
    coverage.addPayor(populateOrganizationReference(coverageDBRecord.getInsureruuid()));

    return coverage;
  }

  private Coverage.CoverageStatus populateStatus(CoverageDBRecord coverageDBRecord) {
    LocalDateTime dateEnd = coverageDBRecord.getDateEnd();
    if (dateEnd == null || dateEnd.isAfter(LocalDateTime.now())) {
      return Coverage.CoverageStatus.ACTIVE;
    }
    return Coverage.CoverageStatus.CANCELLED;
  }

  private CodeableConcept populateType(CoverageDBRecord coverageDBRecord) {
    String type = coverageDBRecord.getType();
    if (type == null || type.isEmpty()) {
      return null;
    }
    Coding coding = new Coding();
    coding.setCode(type.toUpperCase());
    coding.setDisplay(type);
    return new CodeableConcept().addCoding(coding).setText(type);
  }

  private Identifier populateIdentifier(CoverageDBRecord coverageDBRecord) {
    String policyNumber = coverageDBRecord.getPolicyNumber();
    if (policyNumber == null || policyNumber.isEmpty()) {
      return null;
    }
    return new Identifier().setValue(policyNumber);
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
    Period period = new Period();
    if (coverageDBRecord.getDate() != null) {
      period.setStart(toDate(coverageDBRecord.getDate()));
    }
    if (coverageDBRecord.getDateEnd() != null) {
      period.setEnd(toDate(coverageDBRecord.getDateEnd()));
    }
    return period;
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
  }
}

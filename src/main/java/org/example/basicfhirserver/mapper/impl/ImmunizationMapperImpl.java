package org.example.basicfhirserver.mapper.impl;

import static org.example.basicfhirserver.mapper.utils.MapperHelper.getUnknownCodeableConcept;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import org.example.basicfhirserver.mapper.ImmunizationMapper;
import org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants;
import org.example.basicfhirserver.mapper.utils.ProfilesConstants;
import org.example.basicfhirserver.repository.jdbc.immunization.ImmunizationDBRecord;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class ImmunizationMapperImpl implements ImmunizationMapper {

  private static final String COMPLETED_STATUS = "Completed";
  private static final String DEFAULT_OBJECTION_CODE = "PATOBJ";
  private static final String DEFAULT_OBJECTION_DISPLAY = "patient objection";
  private static final String RELIGIOUS_OBJECTION_CODE = "RELIG";
  private static final String RELIGIOUS_OBJECTION_DISPLAY = "religious objection";
  private static final String RELIGIOUS_EXEMPTION_CDC_NIP_CODE = "01";

  @Override
  public Immunization toR4(ImmunizationDBRecord immunizationDBRecord) {
    Immunization immunization = new Immunization();

    immunization.setMeta(populateMeta(immunizationDBRecord));

    immunization.setId(
        immunizationDBRecord.getUuid() != null
            ? immunizationDBRecord.getUuid().toString()
            : immunizationDBRecord.getId().toString());

    immunization.setStatus(populateStatus(immunizationDBRecord));

    CodeableConcept statusReason = populateStatusReason(immunizationDBRecord);
    if (statusReason != null) {
      immunization.setStatusReason(statusReason);
    }

    immunization.setVaccineCode(populateVaccineCode(immunizationDBRecord));
    immunization.setPatient(populatePatientReference(immunizationDBRecord.getPatientPuuid()));

    if (immunizationDBRecord.getEncounterEuuid() != null) {
      immunization.setEncounter(
          new Reference("Encounter/" + immunizationDBRecord.getEncounterEuuid()));
    }

    if (immunizationDBRecord.getAdministeredDate() != null) {
      immunization.setOccurrence(
          new DateTimeType(toDate(immunizationDBRecord.getAdministeredDate())));
    }

    if (immunizationDBRecord.getCreateDate() != null) {
      immunization.setRecorded(toDate(immunizationDBRecord.getCreateDate()));
    }

    immunization.setPrimarySource(immunizationDBRecord.isPrimarySource());

    if (immunizationDBRecord.getManufacturer() != null) {
      immunization.setManufacturer(
          new Reference().setDisplay(immunizationDBRecord.getManufacturer()));
    }

    if (immunizationDBRecord.getLotNumber() != null) {
      immunization.setLotNumber(immunizationDBRecord.getLotNumber());
    }

    if (immunizationDBRecord.getExpirationDate() != null) {
      immunization.setExpirationDate(toDate(immunizationDBRecord.getExpirationDate()));
    }

    CodeableConcept site = populateSite(immunizationDBRecord);
    if (site != null) {
      immunization.setSite(site);
    }

    CodeableConcept route = populateCodeableConcept(immunizationDBRecord.getRoute(), null);
    if (route != null) {
      immunization.setRoute(route);
    }

    Quantity doseQuantity = populateDoseQuantity(immunizationDBRecord);
    if (doseQuantity != null) {
      immunization.setDoseQuantity(doseQuantity);
    }

    populatePerformer(immunization, immunizationDBRecord);
    populateNote(immunization, immunizationDBRecord);
    populateReasonCode(immunization, immunizationDBRecord);
    populateEducation(immunization, immunizationDBRecord);
    populateLocation(immunization, immunizationDBRecord);

    return immunization;
  }

  private Meta populateMeta(ImmunizationDBRecord immunizationDBRecord) {
    Meta meta = new Meta();
    meta.setVersionId("1");
    meta.addProfile(ProfilesConstants.HL7_US_CORE_IMMUNIZATION);
    meta.setLastUpdated(
        immunizationDBRecord.getUpdateDate() != null
            ? toDate(immunizationDBRecord.getUpdateDate())
            : Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
    return meta;
  }

  private Immunization.ImmunizationStatus populateStatus(
      ImmunizationDBRecord immunizationDBRecord) {
    if (immunizationDBRecord.isAddedErroneously()) {
      return Immunization.ImmunizationStatus.ENTEREDINERROR;
    }
    if (COMPLETED_STATUS.equals(immunizationDBRecord.getCompletionStatus())) {
      return Immunization.ImmunizationStatus.COMPLETED;
    }
    return Immunization.ImmunizationStatus.NOTDONE;
  }

  private CodeableConcept populateStatusReason(ImmunizationDBRecord immunizationDBRecord) {
    if (immunizationDBRecord.isAddedErroneously()
        || COMPLETED_STATUS.equals(immunizationDBRecord.getCompletionStatus())) {
      return null;
    }

    String code = DEFAULT_OBJECTION_CODE;
    String display = DEFAULT_OBJECTION_DISPLAY;
    if (RELIGIOUS_EXEMPTION_CDC_NIP_CODE.equals(
        immunizationDBRecord.getRefusalReasonCdcNipCode())) {
      code = RELIGIOUS_OBJECTION_CODE;
      display = RELIGIOUS_OBJECTION_DISPLAY;
    }

    Coding coding = new Coding();
    coding.setSystem(FhirCodeSystemConstants.IMMUNIZATION_OBJECTION_REASON);
    coding.setCode(code);
    coding.setDisplay(display);
    return new CodeableConcept().addCoding(coding);
  }

  private CodeableConcept populateVaccineCode(ImmunizationDBRecord immunizationDBRecord) {
    String cvxCode = immunizationDBRecord.getCvxCode();
    if (cvxCode == null || cvxCode.isEmpty()) {
      return getUnknownCodeableConcept();
    }
    Coding coding = new Coding();
    coding.setSystem(FhirCodeSystemConstants.CVX);
    coding.setCode(cvxCode);
    coding.setDisplay(immunizationDBRecord.getCvxCodeText());
    return new CodeableConcept().addCoding(coding);
  }

  private Reference populatePatientReference(UUID patientUuid) {
    Reference reference = new Reference();
    if (patientUuid != null) {
      reference.setReference("Patient/" + patientUuid);
    }
    return reference;
  }

  private CodeableConcept populateSite(ImmunizationDBRecord immunizationDBRecord) {
    String siteCode = immunizationDBRecord.getSiteCode();
    if (siteCode == null || siteCode.isEmpty()) {
      return null;
    }
    Coding coding = new Coding();
    coding.setSystem(FhirCodeSystemConstants.HL7_V3_ACT_SITE);
    coding.setCode(siteCode);
    coding.setDisplay(immunizationDBRecord.getSiteDisplay());
    return new CodeableConcept().addCoding(coding);
  }

  private CodeableConcept populateCodeableConcept(String code, String display) {
    if (code == null || code.isEmpty()) {
      return null;
    }
    Coding coding = new Coding();
    coding.setCode(code);
    if (display != null && !display.isEmpty()) {
      coding.setDisplay(display);
    }
    return new CodeableConcept().addCoding(coding);
  }

  private Quantity populateDoseQuantity(ImmunizationDBRecord immunizationDBRecord) {
    Integer amountAdministered = immunizationDBRecord.getAmountAdministered();
    if (amountAdministered == null) {
      return null;
    }
    Quantity quantity = new Quantity();
    quantity.setValue(new BigDecimal(amountAdministered));
    quantity.setSystem(FhirCodeSystemConstants.UNITS_OF_MEASURE);
    quantity.setCode(immunizationDBRecord.getAmountAdministeredUnit());
    return quantity;
  }

  private void populatePerformer(
      Immunization immunization, ImmunizationDBRecord immunizationDBRecord) {
    UUID providerUuid = immunizationDBRecord.getProviderUuid();
    String administeredBy = immunizationDBRecord.getAdministeredBy();
    if (providerUuid == null && (administeredBy == null || administeredBy.isEmpty())) {
      return;
    }

    Reference actor = new Reference();
    if (providerUuid != null) {
      actor.setReference("Practitioner/" + providerUuid);
    }
    if (administeredBy != null && !administeredBy.isEmpty()) {
      actor.setDisplay(administeredBy);
    }

    Immunization.ImmunizationPerformerComponent performer =
        new Immunization.ImmunizationPerformerComponent();
    performer.setActor(actor);
    immunization.addPerformer(performer);
  }

  private void populateNote(Immunization immunization, ImmunizationDBRecord immunizationDBRecord) {
    String note = immunizationDBRecord.getNote();
    if (note != null && !note.isEmpty()) {
      immunization.addNote(new Annotation().setText(note));
    }
  }

  private void populateReasonCode(
      Immunization immunization, ImmunizationDBRecord immunizationDBRecord) {
    CodeableConcept reasonCode =
        populateCodeableConcept(immunizationDBRecord.getReasonCode(), null);
    if (reasonCode != null) {
      immunization.addReasonCode(reasonCode);
    }
  }

  private void populateEducation(
      Immunization immunization, ImmunizationDBRecord immunizationDBRecord) {
    if (immunizationDBRecord.getEducationDate() == null) {
      return;
    }
    Immunization.ImmunizationEducationComponent education =
        new Immunization.ImmunizationEducationComponent();
    education.setPresentationDate(toDate(immunizationDBRecord.getEducationDate()));
    immunization.addEducation(education);
  }

  private void populateLocation(
      Immunization immunization, ImmunizationDBRecord immunizationDBRecord) {
    if (immunizationDBRecord.getFacilityLocationUuid() != null) {
      immunization.setLocation(
          new Reference("Location/" + immunizationDBRecord.getFacilityLocationUuid()));
    }
  }

  private Date toDate(LocalDateTime dateTime) {
    return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  private Date toDate(LocalDate date) {
    return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
  }
}

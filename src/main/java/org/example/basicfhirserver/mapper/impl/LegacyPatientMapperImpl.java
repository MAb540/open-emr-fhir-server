package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.example.basicfhirserver.mapper.utils.FhirCodeSystemConstants;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class LegacyPatientMapperImpl implements LegacyPatientMapper {

    private static final String US_CORE_PATIENT_PROFILE =
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient";
    private static final String US_SSN_IDENTIFIER_SYSTEM = "http://hl7.org/fhir/sid/us-ssn";
    private static final String DEFAULT_LANGUAGE_CODE = "en";
    private static final String NARRATIVE_TEMPLATE = "This patient is %s, born in %s, %s.";

    private static final String SEX_MALE_ABBREVIATION = "M";
    private static final String SEX_FEMALE_ABBREVIATION = "F";
    private static final String SEX_UNKNOWN_ABBREVIATION = "U";

    @Override
    public Patient toR4(LegacyPatientEntity legacyPatientEntity) {
        Patient patient = new Patient();

        patient.setMeta(populateMeta(legacyPatientEntity));
        patient.setId(legacyPatientEntity.getUuid().toString());
        patient.setIdentifier(populateIdentifier(legacyPatientEntity));
        patient.setName(populateName(legacyPatientEntity));
        patient.setTelecom(populateTelecom(legacyPatientEntity));
        patient.setAddress(populateAddress(legacyPatientEntity));
        patient.setCommunication(populateCommunication(legacyPatientEntity));
        patient.setText(populateNarrative(legacyPatientEntity));
        patient.setGender(populateGender(legacyPatientEntity));

        if (legacyPatientEntity.getDob() != null) {
            patient.setBirthDate(populateBirthDate(legacyPatientEntity));
        }

        if (legacyPatientEntity.getDeceasedDate() != null) {
            patient.setDeceased(populateDeceasedDateTime(legacyPatientEntity));
        }

        return patient;
    }

    private org.hl7.fhir.r4.model.Meta populateMeta(LegacyPatientEntity legacyPatientEntity) {
        org.hl7.fhir.r4.model.Meta meta = new org.hl7.fhir.r4.model.Meta();
        meta.setVersionId("1");
        meta.addProfile(US_CORE_PATIENT_PROFILE);

        LocalDateTime lastUpdated = legacyPatientEntity.getLastUpdated() != null
                ? legacyPatientEntity.getLastUpdated()
                : LocalDateTime.now();
        meta.setLastUpdated(
                Date.from(lastUpdated.atZone(ZoneId.systemDefault()).toInstant())
        );
        return meta;
    }

    private List<Identifier> populateIdentifier(LegacyPatientEntity legacyPatientEntity) {
        Identifier identifier = new Identifier()
                .setUse(Identifier.IdentifierUse.OFFICIAL)
                .setSystem(US_SSN_IDENTIFIER_SYSTEM)
                .setValue(legacyPatientEntity.getSs());

        return List.of(identifier);
    }

    private List<HumanName> populateName(LegacyPatientEntity legacyPatientEntity) {
        HumanName name = new HumanName()
                .setFamily(legacyPatientEntity.getLname())
                .addGiven(legacyPatientEntity.getFname())
                .setText(legacyPatientEntity.getLname() + " " + legacyPatientEntity.getFname())
                .setUse(HumanName.NameUse.OFFICIAL);

        return List.of(name);
    }

    private List<ContactPoint> populateTelecom(LegacyPatientEntity legacyPatientEntity) {
        return List.of(
                newContactPoint(ContactPoint.ContactPointSystem.PHONE,
                        ContactPoint.ContactPointUse.MOBILE, legacyPatientEntity.getPhoneContact()),
                newContactPoint(ContactPoint.ContactPointSystem.PHONE,
                        ContactPoint.ContactPointUse.HOME, legacyPatientEntity.getPhoneHome()),
                newContactPoint(ContactPoint.ContactPointSystem.PHONE,
                        ContactPoint.ContactPointUse.HOME, legacyPatientEntity.getPhoneBiz()),
                newContactPoint(ContactPoint.ContactPointSystem.EMAIL,
                        ContactPoint.ContactPointUse.HOME, legacyPatientEntity.getEmail())
        );
    }

    private ContactPoint newContactPoint(ContactPoint.ContactPointSystem system,
                                         ContactPoint.ContactPointUse use,
                                         String value) {
        ContactPoint contactPoint = new ContactPoint();
        contactPoint.setSystem(system)
                .setValue(value)
                .setUse(use);
        return contactPoint;
    }

    private Date populateBirthDate(LegacyPatientEntity legacyPatientEntity) {
        return Date.from(legacyPatientEntity.getDob().atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private List<Address> populateAddress(LegacyPatientEntity legacyPatientEntity) {
        Address address = new Address();
        address.setUse(Address.AddressUse.HOME)
                .setType(Address.AddressType.BOTH)
                .setText(legacyPatientEntity.getStreet() + legacyPatientEntity.getCity() + legacyPatientEntity.getState())
                .setLine(List.of(new StringType(legacyPatientEntity.getStreet())))
                .setCity(legacyPatientEntity.getCity())
                .setState(legacyPatientEntity.getState())
                .setPostalCode(legacyPatientEntity.getPostalCode())
                .setCountry(legacyPatientEntity.getCountryCode());

        return List.of(address);
    }

    private List<Patient.PatientCommunicationComponent> populateCommunication(
            LegacyPatientEntity legacyPatientEntity) {
        Patient.PatientCommunicationComponent communication = new Patient.PatientCommunicationComponent();
        communication.setPreferred(true);
        communication.setLanguage(new CodeableConcept().addCoding(
                new Coding().setSystem(FhirCodeSystemConstants.LANGUAGE_BCP_47)
                        .setCode(DEFAULT_LANGUAGE_CODE)
                        .setDisplay(legacyPatientEntity.getLanguage())
        ));

        return List.of(communication);
    }

    private Narrative populateNarrative(LegacyPatientEntity legacyPatientEntity) {
        Narrative narrative = new Narrative()
                .setStatus(Narrative.NarrativeStatus.GENERATED)
                .setDiv(new XhtmlNode(NodeType.Element, "div")
                        .setValue(String.format(
                                NARRATIVE_TEMPLATE,
                                legacyPatientEntity.getFname(),
                                legacyPatientEntity.getCity() + legacyPatientEntity.getState(),
                                legacyPatientEntity.getCountryCode())));

        return narrative;
    }

    private Type populateDeceasedDateTime(LegacyPatientEntity legacyPatientEntity) {
        return new DateTimeType(
                Date.from(legacyPatientEntity.getDeceasedDate().atZone(ZoneId.systemDefault()).toInstant())
        );
    }

    private Enumerations.AdministrativeGender populateGender(LegacyPatientEntity legacyPatientEntity) {
        String sex = legacyPatientEntity.getSex();

        if (Objects.equals(sex, SEX_MALE_ABBREVIATION) || Objects.equals(sex, "Male")) {
            return Enumerations.AdministrativeGender.MALE;
        }
        if (Objects.equals(sex, SEX_FEMALE_ABBREVIATION) || Objects.equals(sex, "Female")) {
            return Enumerations.AdministrativeGender.FEMALE;
        }
        if (Objects.equals(sex, SEX_UNKNOWN_ABBREVIATION) || Objects.equals(sex, "Unknown")) {
            return Enumerations.AdministrativeGender.UNKNOWN;
        }

        return Enumerations.AdministrativeGender.NULL;
    }

}

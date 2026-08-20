package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.domain.entities.LegacyPatientEntity;
import org.example.basicfhirserver.mapper.LegacyPatientMapper;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;


@Component
public class LegacyPatientMapperImpl implements LegacyPatientMapper {

    public Patient toR4(LegacyPatientEntity legacyPatientEntity) {
        // 🛠️ REVERSE TRANSFORM: Map the generic legacy record back to clean FHIR R4 JSON
        Patient fhirPatient = new Patient();

        fhirPatient.getMeta().addProfile(
                "http://hl7.org/fhir/us/core/StructureDefinition/us-core-patient"
        );
        fhirPatient.getMeta().setVersionId("1");

        fhirPatient.setId(legacyPatientEntity.getId().toString());

        Identifier identifier = new Identifier()
                .setUse(Identifier.IdentifierUse.OFFICIAL)
                .setSystem("http://hl7.org/fhir/sid/us-ssn")
                .setValue(legacyPatientEntity.getSs());
        fhirPatient.setIdentifier(Collections.singletonList(identifier));

        HumanName name = new HumanName()
                .setFamily(legacyPatientEntity.getLname())
                .addGiven(legacyPatientEntity.getFname())
                .setText(legacyPatientEntity.getLname() + " " + legacyPatientEntity.getFname())
                .setUse(HumanName.NameUse.OFFICIAL);
        fhirPatient.setName(Collections.singletonList(name));

        ContactPoint contactPointPhone1 = new ContactPoint();
        contactPointPhone1.setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue(legacyPatientEntity.getPhoneContact())
                .setUse(ContactPoint.ContactPointUse.MOBILE);

        ContactPoint contactPointPhone2 = new ContactPoint();
        contactPointPhone2.setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue(legacyPatientEntity.getPhoneHome())
                .setUse(ContactPoint.ContactPointUse.HOME);

        ContactPoint contactPointPhone3 = new ContactPoint();
        contactPointPhone3.setSystem(ContactPoint.ContactPointSystem.PHONE)
                .setValue(legacyPatientEntity.getPhoneBiz())
                .setUse(ContactPoint.ContactPointUse.HOME);

        ContactPoint contactPointEmail = new ContactPoint();
        contactPointEmail.setSystem(ContactPoint.ContactPointSystem.EMAIL)
                .setValue(legacyPatientEntity.getEmail())
                .setUse(ContactPoint.ContactPointUse.HOME);

        fhirPatient.setTelecom(List.of(contactPointPhone1, contactPointPhone2, contactPointPhone3, contactPointEmail));

        if (legacyPatientEntity.getDob() != null) {
            fhirPatient.setBirthDate(Date.from(legacyPatientEntity.getDob().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        Address address = new Address();
        address.setUse(Address.AddressUse.HOME)
                .setType(Address.AddressType.BOTH)
                .setText(legacyPatientEntity.getStreet() + legacyPatientEntity.getCity() + legacyPatientEntity.getState())
                .setLine(List.of(new StringType(legacyPatientEntity.getStreet())))
                .setCity(legacyPatientEntity.getCity())
                .setState(legacyPatientEntity.getState())
                .setPostalCode(legacyPatientEntity.getPostalCode())
                .setCountry(legacyPatientEntity.getCountryCode());

        fhirPatient.setAddress(List.of(address));
        fhirPatient.addCommunication()
                .setLanguage(new CodeableConcept().addCoding(
                        new Coding().setSystem("urn:ietf:bcp:47")
                                .setCode("en")
                                .setDisplay(legacyPatientEntity.getLanguage())
                )).setPreferred(true);

        Narrative narrative = new Narrative()
                .setStatus(Narrative.NarrativeStatus.GENERATED)
                .setDiv(new XhtmlNode(NodeType.Element, "div")
                        .setValue(String.format("This patient is %s, born in %s , $s.", legacyPatientEntity.getFname(),
                                legacyPatientEntity.getCity() + legacyPatientEntity.getState()
                                , legacyPatientEntity.getCountryCode())));
        fhirPatient.setText(narrative);

        fhirPatient.setGender(mapGender(legacyPatientEntity.getSex()));

        LocalDateTime localDateTime = legacyPatientEntity.getDeceasedDate();
        if (localDateTime != null) {
            fhirPatient.setDeceased(new DateTimeType(Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())));
        }

        return fhirPatient;
    }

    private Enumerations.AdministrativeGender mapGender(String gender) {
        if (Objects.equals(gender, "M") || Objects.equals(gender, "Male")) {
            return Enumerations.AdministrativeGender.MALE;
        }
        if (Objects.equals(gender, "F") || Objects.equals(gender, "Female")) {
            return Enumerations.AdministrativeGender.FEMALE;
        }
        if (Objects.equals(gender, "U") || Objects.equals(gender, "Unknown")) {
            return Enumerations.AdministrativeGender.UNKNOWN;
        }

        return Enumerations.AdministrativeGender.NULL;
    }

}

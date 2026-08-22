package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.PractitionerMapper;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class PractitionerMapperImpl implements PractitionerMapper {

    @Override
    public Practitioner toR4(org.example.basicfhirserver.model.Practitioner userPractitioner) {
        Practitioner practitioner = new Practitioner();

        practitioner.getMeta()
                .addProfile("http://hl7.org/fhir/us/core/StructureDefinition/us-core-practitioner")
                .setVersionId("1")
                .setLastUpdated(
                        Date.from(userPractitioner.getLastUpdated().atZone(ZoneId.systemDefault()).toInstant())
                );

        practitioner.setId(userPractitioner.getUuid().toString());
        practitioner.setActive(userPractitioner.getActive());
        practitioner.setName(Collections.singletonList(createHumanNameFromRecord(userPractitioner)));

        Narrative narrative = new Narrative()
                .setStatus(Narrative.NarrativeStatus.GENERATED)
                .setDiv(new XhtmlNode(NodeType.Element, "div")
                        .setValue(userPractitioner.getFirstName() + " " + userPractitioner.getLastName()));
        practitioner.setText(narrative);

        practitioner.setAddress(normalizeAddress(userPractitioner));
        practitioner.setTelecom(normalizeContactPoint(userPractitioner));

        if (userPractitioner.getNpi() != null) {
            practitioner.addIdentifier(
                    new Identifier()
                            .setSystem("http://hl7.org/fhir/sid/us-npi")
                            .setValue(userPractitioner.getNpi())
            );
        } else {
            Identifier missingIdentifier = new Identifier();
            missingIdentifier.setSystem("http://hl7.org/fhir/sid/us-npi");
            missingIdentifier.getValueElement().addExtension(
                    new Extension()
                            .setUrl("http://hl7.org/fhir/StructureDefinition/data-absent-reason")
                            .setValue(new CodeType("unknown"))
            );
            practitioner.addIdentifier(missingIdentifier);
        }


        return practitioner;

    }

    private List<ContactPoint> normalizeContactPoint(org.example.basicfhirserver.model.Practitioner userPractitioner) {

        List<org.example.basicfhirserver.model.Practitioner.TelecomItem> telecomItems = userPractitioner.getTelecoms();
        if (telecomItems == null) {
            return new java.util.ArrayList<>();
        }

        return telecomItems.stream().map(item -> {
            ContactPoint contactPointPhone = new ContactPoint();

            if (item.getSystem() != null) {
                String systemStr = item.getSystem().toLowerCase().trim();
                try {
                    contactPointPhone.setSystem(ContactPoint.ContactPointSystem.fromCode(systemStr));
                } catch (Exception e) {
                    contactPointPhone.setSystem(ContactPoint.ContactPointSystem.PHONE);
                }
            }

            if (item.getUse() != null) {
                String useStr = item.getUse().toLowerCase().trim();
                try {
                    contactPointPhone.setUse(ContactPoint.ContactPointUse.fromCode(useStr));
                } catch (Exception e) {
                    contactPointPhone.setUse(ContactPoint.ContactPointUse.WORK);
                }
            }
            contactPointPhone.setValue(item.getValue());
            return contactPointPhone;
        }).toList();
    }

    private List<Address> normalizeAddress(org.example.basicfhirserver.model.Practitioner userPractitioner) {
        if (userPractitioner == null || userPractitioner.getAddresses() == null) {
            return new java.util.ArrayList<>();
        }

        return userPractitioner.getAddresses().stream()
                .filter(java.util.Objects::nonNull)
                .map(item -> {
                    Address address = new Address();

                    List<StringType> lines = java.util.stream.Stream.of(item.getLine1(), item.getLine2())
                            .filter(line -> line != null && !line.trim().isEmpty())
                            .map(StringType::new)
                            .toList();

                    if (!lines.isEmpty()) {
                        address.setLine(lines);
                    }

                    address.setCity(item.getCity());
                    address.setState(item.getState());
                    address.setPostalCode(item.getPostalCode());
                    address.setCountry(item.getCountry());

                    return address;
                })
                .toList();
    }

    private HumanName createHumanNameFromRecord(org.example.basicfhirserver.model.Practitioner userPractitioner) {
        HumanName name = new HumanName();
        name.setUse(HumanName.NameUse.OFFICIAL);

        if (userPractitioner.getPhysicianTypeTitle() != null && !userPractitioner.getPhysicianTypeTitle().trim().isEmpty()) {
            name.addPrefix(userPractitioner.getPhysicianTypeTitle());
        }

        if (userPractitioner.getLastName() != null && !userPractitioner.getLastName().trim().isEmpty()) {
            name.setFamily(userPractitioner.getLastName());
        }

        if (userPractitioner.getFirstName() != null && !userPractitioner.getFirstName().trim().isEmpty()) {
            name.addGiven(userPractitioner.getFirstName());
        }

        if (userPractitioner.getMiddleName() != null && !userPractitioner.getMiddleName().trim().isEmpty()) {
            name.addGiven(userPractitioner.getMiddleName());
        }

        String textLname = userPractitioner.getLastName() != null ? userPractitioner.getLastName() : "";
        String textFname = userPractitioner.getFirstName() != null ? userPractitioner.getFirstName() : "";
        name.setText((textLname + " " + textFname).trim());

        return name;
    }

}

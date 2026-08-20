package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.EncounterMapper;
import org.example.basicfhirserver.model.FormEncounter;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component
public class EncounterMapperImpl implements EncounterMapper {

    @Override
    public Encounter toR4(FormEncounter formEncounter) {

        Encounter encounter = new Encounter();
        encounter.getMeta().addProfile(
                "http://hl7.org/fhir/us/core/StructureDefinition/us-core-encounter"
        );
        encounter.getMeta().setVersionId("1");
        encounter.getMeta().setLastUpdated(
                Date.from(formEncounter.getLastUpdate().atZone(ZoneId.systemDefault()).toInstant())
        );
        encounter.setId(formEncounter.getUuid().toString());

        Identifier identifier = new Identifier()
                .setUse(Identifier.IdentifierUse.OFFICIAL)
                .setSystem("urn:uuid:" + formEncounter.getUuid().toString())
                .setValue(formEncounter.getUuid().toString());
        encounter.setIdentifier(List.of(identifier));

        encounter.setStatus(Encounter.EncounterStatus.FINISHED);

        if (formEncounter.getEncounterClass().getCode() != null) {
            encounter.setClass_(
                    new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode")
                            .setCode(formEncounter.getEncounterClass().getCode())
                            .setDisplay(formEncounter.getEncounterClass().getTitle())
            );
        } else {
            encounter.setClass_(
                    new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/data-absent-reason")
                            .setCode("unknown")
                            .setDisplay("Unknown")
            );
        }
        encounter.addType(
                new CodeableConcept()
                        .setText("Encounter for check up (procedure)")
                        .addCoding(
                                new Coding()
                                        .setSystem("http://snomed.info/sct")
                                        .setCode("185349003")
                                        .setDisplay("Encounter for check up (procedure)")
                        )
        );

        encounter.setSubject(
                new Reference("Patient/" + formEncounter.getPatient().getUuid())
        );

        if (formEncounter.getProviders().getOrderingProviderId() != null) {
            encounter.addParticipant(
                    new Encounter.EncounterParticipantComponent()
                            .setIndividual(
                                    new Reference("Practitioner/" + formEncounter.getProviders().getOrderingProviderId())
                            )
                            .setPeriod(
                                    new Period()
                                            .setStart(
                                                    Date.from(formEncounter.getDate().atZone(ZoneId.systemDefault()).toInstant())
                                            )
                            )
                            .setType(
                                    List.of(
                                            new CodeableConcept()
                                                    .addCoding(
                                                            new Coding()
                                                                    .setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType")
                                                                    .setCode("PPRF")
                                                                    .setDisplay("Primary Performer")
                                                    )
                                    )
                            )
            );
        }

        encounter
                .setPeriod(
                        new Period()
                                .setStart(
                                        Date.from(formEncounter.getDate().atZone(ZoneId.systemDefault()).toInstant())
                                )
                );

        encounter.setReasonCode(
                List.of(
                        new CodeableConcept()
                                .setText(formEncounter.getReason())

                )
        );

        if (formEncounter.getDischarge() != null) {
            encounter.setHospitalization(
                    new Encounter.EncounterHospitalizationComponent()
                            .setDischargeDisposition(
                                    new CodeableConcept()
                                            .addCoding(
                                                    new Coding()
                                                            .setSystem("http://terminology.hl7.org/CodeSystem/discharge-disposition")
                                                            .setCode(formEncounter.getDischarge().getDispositionCode()
                                                            )
                                                            .setDisplay(
                                                                    formEncounter.getDischarge().getDispositionText()
                                                            )
                                            )
                            )
            );
        }


        encounter.setServiceProvider(
                new Reference("Organization/" + formEncounter.getServiceFacility().getUuid())
        );

        encounter.addLocation(
                new Encounter.EncounterLocationComponent()
                        .setLocation(
                                new Reference("Location/" + formEncounter.getServiceFacility().getLocationUuid())
                        )
        );

        return encounter;
    }
}

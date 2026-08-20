package org.example.basicfhirserver.mapper.impl;

import org.example.basicfhirserver.mapper.ObservationMapper;
import org.example.basicfhirserver.model.VitalObservation;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.example.basicfhirserver.service.assembler.vitals.VitalObservationType.*;


@Component
public class ObservationMapperImpl implements ObservationMapper {

    @Override
    public Observation toR4(VitalObservation vitalObservation) {
        Observation observation = new Observation();

        observation.getMeta().addProfile(
                vitalObservation.getProfile()
        ).setLastUpdated(
                Date.from(vitalObservation.getLastUpdated().atZone(ZoneId.systemDefault()).toInstant())
        ).setVersionId(vitalObservation.getVersion());


        observation.setId(vitalObservation.getId());
        observation.setStatus(
                Observation.ObservationStatus.FINAL
        );
        observation.addCategory(
                new CodeableConcept()
                        .addCoding(
                                new Coding()
                                        .setSystem(
                                                "http://terminology.hl7.org/CodeSystem/observation-category"
                                        )
                                        .setCode("vital-signs")
                                        .setDisplay("Vital Signs")
                        )
        );
        observation.setCode(
                new CodeableConcept()
                        .addCoding(
                                new Coding()
                                        .setSystem("http://loinc.org")
                                        .setCode(vitalObservation.getCode())
                                        .setDisplay(vitalObservation.getDisplay())
                        )
        );
        observation.setSubject(
                new Reference("Patient/" + vitalObservation.getPatientId())
        );
        observation.setEffective(
                new DateTimeType(
                        vitalObservation.getEffectiveDateTime().toString()
                )
        );
        observation.setEncounter(
                new Reference("Encounter/" + vitalObservation.getEncounterId())
        );

        UnitMapping result = mapUnitAndCode(vitalObservation.getUnit());
        observation.setPerformer(
                List.of(new Reference("Practitioner/" + vitalObservation.getPractitionerId()))
        );

        if (vitalObservation.getCode().equals(BLOOD_PRESSURE.getCode())) {

            BigDecimal systolicBp = vitalObservation.getComponents().get(SYSTOLIC_BP.getCode()).value();
            BigDecimal diastolicBp = vitalObservation.getComponents().get(DIASTOLIC_BP.getCode()).value();

            if (systolicBp.compareTo(BigDecimal.ZERO) == 0 && diastolicBp.compareTo(BigDecimal.ZERO) == 0) {
                observation.setDataAbsentReason(new CodeableConcept()
                        .setText("Unknown")
                        .addCoding(
                                new Coding()
                                        .setSystem("http://terminology.hl7.org/CodeSystem/data-absent-reason")
                                        .setCode("unknown")
                        )
                );
            } else {
                Observation.ObservationComponentComponent systolicComp = new Observation.ObservationComponentComponent();
                systolicComp.getCode()
                        .addCoding(
                                new Coding()
                                        .setSystem("http://loinc.org")
                                        .setCode(SYSTOLIC_BP.getCode())
                                        .setDisplay(SYSTOLIC_BP.getDisplay())
                        );
                systolicComp.setValue(
                        new Quantity()
                                .setValue(systolicBp)
                                .setUnit(SYSTOLIC_BP.getUnit())
                                .setSystem("http://unitsofmeasure.org")
                                .setCode(SYSTOLIC_BP.getUnit())
                );


                Observation.ObservationComponentComponent diastolicComp = new Observation.ObservationComponentComponent();
                diastolicComp.getCode()
                        .addCoding(
                                new Coding()
                                        .setSystem("http://loinc.org")
                                        .setCode(DIASTOLIC_BP.getCode())
                                        .setDisplay(DIASTOLIC_BP.getDisplay())
                        );
                diastolicComp.setValue(
                        new Quantity()
                                .setValue(vitalObservation.getComponents().get(DIASTOLIC_BP.getCode()).value())
                                .setUnit(DIASTOLIC_BP.getUnit())
                                .setSystem("http://unitsofmeasure.org")
                                .setCode(DIASTOLIC_BP.getUnit())
                );
                observation.setComponent(List.of(systolicComp, diastolicComp));
            }

        } else {
            observation.setValue(
                    new Quantity()
                            .setValue(vitalObservation.getValue())
                            .setUnit(result.unit())
                            .setSystem("http://unitsofmeasure.org")
                            .setCode(result.code())
            );
        }

        return observation;
    }


    public record UnitMapping(String unit, String code) {
    }

    private UnitMapping mapUnitAndCode(String inputUnit) {
        String unit = "";
        String code = "";

        if ("in".equals(inputUnit)) {
            unit = "in_i";
            code = "[" + unit + "]";
        } else if ("cm".equals(inputUnit)) {
            unit = "cm";
            code = "cm";
        } else if ("lb".equals(inputUnit)) {
            unit = "lb_av";
            code = "[" + unit + "]";
        } else if ("kg".equals(inputUnit)) {
            unit = "kg";
            code = "kg";
        } else if ("degF".equals(inputUnit)) {
            unit = "degF";
            code = "[" + inputUnit + "]";
        } else if ("Cel".equals(inputUnit)) {
            unit = "Cel";
            code = "Cel";
        } else {
            unit = inputUnit;
            code = inputUnit;
        }

        return new UnitMapping(unit, code);
    }

}

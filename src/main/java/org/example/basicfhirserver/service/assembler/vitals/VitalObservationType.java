package org.example.basicfhirserver.service.assembler.vitals;

import lombok.Getter;

@Getter
public enum VitalObservationType {
    BODY_WEIGHT(
            "29463-7",
            "Body weight",
            "kg",
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-body-weight"
    ),
    BODY_HEIGHT(
            "8302-2",
            "Body height",
            "cm",
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-body-height"
    ),
    BODY_TEMPERATURE(
            "8310-5",
            "Body temperature",
            "Cel",
            "http://hl7.org/fhir/us/core/StructureDefinition/us-core-body-temperature"
    ),
    BMI(
            "39156-5",
                    "Body mass index (BMI) [Ratio]",
                    "kg/m2",
                    "http://hl7.org/fhir/us/core/StructureDefinition/us-core-bmi"
    ),
   BLOOD_PRESSURE(
            "85354-9",
                    "Blood pressure systolic and diastolic",
                    "",
                    "http://hl7.org/fhir/us/core/StructureDefinition/us-core-blood-pressure"
    ),
    SYSTOLIC_BP(
            "8480-6",
            "Systolic blood pressure",
            "mm[Hg]",
            ""
    ),
    DIASTOLIC_BP(
            "8462-4",
                    "Diastolic blood pressure",
                    "mm[Hg]",
                    ""
    );

    private final String code;
    private final String display;
    private final String unit;
    private final String profile;


    VitalObservationType(String code, String display, String unit, String profile) {
        this.code = code;
        this.display = display;
        this.unit = unit;
        this.profile = profile;
    }

    public static VitalObservationType fromCode(String code) {

        if (code == null) {
            return null;
        }

        for (VitalObservationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }

        return null;
    }
}

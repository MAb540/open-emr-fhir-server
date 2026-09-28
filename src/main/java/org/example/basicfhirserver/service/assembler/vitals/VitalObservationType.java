package org.example.basicfhirserver.service.assembler.vitals;

import lombok.Getter;

import static org.example.basicfhirserver.mapper.utils.ProfilesConstants.HL7_US_CORE_BLOOD_PRESSURE;
import static org.example.basicfhirserver.mapper.utils.ProfilesConstants.HL7_US_CORE_BMI;
import static org.example.basicfhirserver.mapper.utils.ProfilesConstants.HL7_US_CORE_BODY_HEIGHT;
import static org.example.basicfhirserver.mapper.utils.ProfilesConstants.HL7_US_CORE_BODY_TEMPERATURE;
import static org.example.basicfhirserver.mapper.utils.ProfilesConstants.HL7_US_CORE_BODY_WEIGHT;

@Getter
public enum VitalObservationType {
    BODY_WEIGHT(
            "29463-7",
            "Body weight",
            "kg",
            HL7_US_CORE_BODY_WEIGHT
    ),
    BODY_HEIGHT(
            "8302-2",
            "Body height",
            "cm",
            HL7_US_CORE_BODY_HEIGHT
    ),
    BODY_TEMPERATURE(
            "8310-5",
            "Body temperature",
            "Cel",
            HL7_US_CORE_BODY_TEMPERATURE
    ),
    BMI(
            "39156-5",
                    "Body mass index (BMI) [Ratio]",
                    "kg/m2",
                    HL7_US_CORE_BMI
    ),
   BLOOD_PRESSURE(
            "85354-9",
                    "Blood pressure systolic and diastolic",
                    "",
                    HL7_US_CORE_BLOOD_PRESSURE
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

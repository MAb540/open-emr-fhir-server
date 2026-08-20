package org.example.basicfhirserver.service.assembler.vitals;


import java.util.Objects;

import static org.example.basicfhirserver.service.assembler.vitals.VitalObservationType.*;

public class ObservationUuidUtil {

    public static String getCode(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }

        for (String parameter : resourcePath.split("&")) {
            if (parameter.startsWith("code=")) {
                return parameter.substring("code=".length());
            }
        }

        return null;
    }

    public static VitalObservationType getObservationFromCode(String resourcePath) {
        String code = getCode(resourcePath);

        VitalObservationType val = null;
        if(Objects.equals(code, BODY_WEIGHT.getCode())){
            val = BODY_WEIGHT;
        }
        if(Objects.equals(code, BODY_HEIGHT.getCode())){
            val = BODY_HEIGHT;
        }
        if(Objects.equals(code, BODY_TEMPERATURE.getCode())){
            val = BODY_TEMPERATURE;
        }

        if(Objects.equals(code, BMI.getCode())){
            val = BMI;
        }

        if(Objects.equals(code, BLOOD_PRESSURE.getCode())){
            val = BLOOD_PRESSURE;
        }

        return val;
    }
}
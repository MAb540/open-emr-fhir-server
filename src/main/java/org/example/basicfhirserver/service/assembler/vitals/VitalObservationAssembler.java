package org.example.basicfhirserver.service.assembler.vitals;

import org.example.basicfhirserver.domain.entities.FormVitalsEntity;
import org.example.basicfhirserver.model.VitalObservation;
import org.example.basicfhirserver.repository.jdbc.vitals.VitalsDBRecord;
import org.example.basicfhirserver.repository.jdbc.vitals.VitalsUuidMappingDBRecord;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.example.basicfhirserver.service.assembler.vitals.VitalObservationType.*;

@Component
public class VitalObservationAssembler {

    public VitalObservation from(
            FormVitalsEntity vitals,
            VitalObservationType type,
            UUID observationUuid,
            UUID patientUuid,
            UUID encounterUuid,
            UUID userUuid
    ) {

        VitalObservation.VitalObservationBuilder builder = VitalObservation.builder().
                id(observationUuid.toString())
                .patientId(patientUuid.toString())
                .encounterId(encounterUuid.toString())
                .effectiveDateTime(vitals.getDate())
                .profile(type.getProfile())
                .practitionerId(userUuid.toString())
                .lastUpdated(vitals.getLastUpdated())
                .version("1");

        switch (type) {
            case BODY_WEIGHT:
                return builder.value(MeasurementUtils.lbToKg(vitals.getWeight()))
                        .unit(BODY_WEIGHT.getUnit())
                        .code(BODY_WEIGHT.getCode())
                        .display(BODY_WEIGHT.getDisplay())
                        .build();

            case BODY_HEIGHT:
                return builder.value(
                                MeasurementUtils.inchesToCm(vitals.getHeight()))
                        .unit(BODY_HEIGHT.getUnit())
                        .code(BODY_HEIGHT.getCode())
                        .display(BODY_HEIGHT.getDisplay())
                        .build();

            case BODY_TEMPERATURE:
                return builder.value(MeasurementUtils.fhToCelsius(vitals.getTemperature()))
                        .unit(BODY_TEMPERATURE.getUnit())
                        .code(BODY_TEMPERATURE.getCode())
                        .display(BODY_TEMPERATURE.getDisplay())
                        .build();

            case BMI:
                return builder.value(vitals.getBmi())
                        .unit(BMI.getUnit())
                        .code(BMI.getCode())
                        .display(BMI.getDisplay())
                        .build();

            case BLOOD_PRESSURE:
                return builder
                        .code(BLOOD_PRESSURE.getCode())
                        .display(BLOOD_PRESSURE.getDisplay())
                        .components(getComponentsMap(vitals))
                        .build();
            default:
                throw new IllegalArgumentException("Unsupported vital observation type: " + type);
        }

    }

    public List<VitalObservation> from(FormVitalsEntity vitals,
                                       Map<String, UUID> codeMappings,
                                       UUID patientUuid,
                                       UUID encounterUuid,
                                       UUID userUuid
    ) {
        List<VitalObservation> observations = new ArrayList<>();
        if (vitals.getWeight() != null) {
            observations.add(from(vitals, BODY_WEIGHT,
                    codeMappings.get(BODY_WEIGHT.getCode()),
                    patientUuid,
                    encounterUuid,
                    userUuid));
        }
        if (vitals.getHeight() != null) {
            observations.add(from(vitals, BODY_HEIGHT,
                    codeMappings.get(BODY_HEIGHT.getCode()),
                    patientUuid,
                    encounterUuid,
                    userUuid));
        }

        if (vitals.getTemperature() != null) {
            observations.add(from(vitals, BODY_TEMPERATURE,
                    codeMappings.get(BODY_TEMPERATURE.getCode()),
                    patientUuid,
                    encounterUuid,
                    userUuid));
        }

        if (vitals.getBmi() != null) {
            observations.add(from(vitals, BMI,
                    codeMappings.get(BMI.getCode()),
                    patientUuid,
                    encounterUuid,
                    userUuid));
        }

        if (vitals.getBps() != null & vitals.getBpd() != null) {
            observations.add(from(vitals, BLOOD_PRESSURE,
                    codeMappings.get(BLOOD_PRESSURE.getCode()),
                    patientUuid,
                    encounterUuid,
                    userUuid));
        }

        return observations;
    }


    public List<VitalObservation> toCanonical(
            VitalsDBRecord row,
            List<VitalsUuidMappingDBRecord> mappings
    ) {
        List<VitalObservation> observations = new ArrayList<>();

        for (VitalsUuidMappingDBRecord mapping : mappings) {
            VitalObservationType type =
                    ObservationUuidUtil.getObservationFromCode(
                            mapping.getResourcePath()
                    );

            if (type == null) {
                continue;
            }

            UUID observationUuid = mapping.getUuid();

            VitalObservation.VitalObservationBuilder builder =
                    VitalObservation.builder()
                            .id(observationUuid.toString())
                            .patientId(row.getPatientUuid().toString())
                            .encounterId(row.getEncounterUuid().toString())
                            .effectiveDateTime(row.getEffectiveDateTime())
                            .profile(type.getProfile())
                            .practitionerId(row.getPractitionerUuid().toString())
                            .lastUpdated(row.getLastUpdated())
                            .version("1");

            VitalObservation observation = null;

            switch (type) {

                case BODY_WEIGHT:
                    if (row.getWeight() != null) {
                        observation = builder
                                .value(
                                        MeasurementUtils.lbToKg(
                                                row.getWeight()
                                        )
                                )
                                .unit(BODY_WEIGHT.getUnit())
                                .code(BODY_WEIGHT.getCode())
                                .display(BODY_WEIGHT.getDisplay())
                                .build();
                    }
                    break;

                case BODY_HEIGHT:
                    if (row.getHeight() != null) {
                        observation = builder
                                .value(
                                        MeasurementUtils.inchesToCm(
                                                row.getHeight()
                                        )
                                )
                                .unit(BODY_HEIGHT.getUnit())
                                .code(BODY_HEIGHT.getCode())
                                .display(BODY_HEIGHT.getDisplay())
                                .build();
                    }
                    break;

                case BODY_TEMPERATURE:
                    if (row.getTemperature() != null) {
                        observation = builder
                                .value(
                                        MeasurementUtils.fhToCelsius(
                                                row.getTemperature()
                                        )
                                )
                                .unit(BODY_TEMPERATURE.getUnit())
                                .code(BODY_TEMPERATURE.getCode())
                                .display(BODY_TEMPERATURE.getDisplay())
                                .build();
                    }
                    break;

                case BMI:
                    if (row.getBmi() != null) {
                        observation = builder
                                .value(row.getBmi())
                                .unit(BMI.getUnit())
                                .code(BMI.getCode())
                                .display(BMI.getDisplay())
                                .build();
                    }
                    break;

                case BLOOD_PRESSURE:
                    if (row.getSystolic() != null || row.getDiastolic() != null) {
                        observation = builder
                                .code(BLOOD_PRESSURE.getCode())
                                .display(BLOOD_PRESSURE.getDisplay())
                                .components(
                                        getComponentsMap(
                                                row.getSystolic(),
                                                row.getDiastolic()
                                        )
                                )
                                .build();
                    }
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unsupported vital observation type: " + type
                    );
            }

            if (observation != null) {
                observations.add(observation);
            }
        }

        return observations;
    }

    private static @NonNull Map<String, VitalObservation.VitalObservationComponent> getComponentsMap(BigDecimal bps, BigDecimal bpd) {
        VitalObservation.VitalObservationComponent systolic =
                new VitalObservation.VitalObservationComponent(
                        bps
                );
        VitalObservation.VitalObservationComponent diastolic =
                new VitalObservation.VitalObservationComponent(
                        bpd
                );

        return Map.of(
                SYSTOLIC_BP.getCode(), systolic,
                DIASTOLIC_BP.getCode(), diastolic
        );
    }


    private static @NonNull Map<String, VitalObservation.VitalObservationComponent> getComponentsMap(FormVitalsEntity vitals) {
        VitalObservation.VitalObservationComponent systolic =
                new VitalObservation.VitalObservationComponent(
                        new BigDecimal(vitals.getBps())
                );
        VitalObservation.VitalObservationComponent diastolic =
                new VitalObservation.VitalObservationComponent(
                        new BigDecimal(vitals.getBpd())
                );

        return Map.of(
                SYSTOLIC_BP.getCode(), systolic,
                DIASTOLIC_BP.getCode(), diastolic
        );
    }


    private static class MeasurementUtils {

        private static final int MEASUREMENT_PRECISION = 6;

        public static BigDecimal lbToKg(BigDecimal val) {
            return val.multiply(BigDecimal.valueOf(0.45359237));
        }

        public static BigDecimal inchesToCm(BigDecimal val) {
            return val.multiply(BigDecimal.valueOf(2.54));
        }

        public static BigDecimal fhToCelsius(BigDecimal val) {
            BigDecimal start = val.subtract(BigDecimal.valueOf(32));
            BigDecimal factor = BigDecimal.valueOf(5).divide(BigDecimal.valueOf(9), MEASUREMENT_PRECISION + 2, RoundingMode.HALF_UP);
            return start.multiply(factor);
        }

    }

}
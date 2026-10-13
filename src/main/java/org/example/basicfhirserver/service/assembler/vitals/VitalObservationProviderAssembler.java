package org.example.basicfhirserver.service.assembler.vitals;

import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.repository.jdbc.vitals.VitalsDBRecord;
import org.example.basicfhirserver.repository.jdbc.vitals.VitalsUuidMappingDBRecord;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.example.basicfhirserver.service.assembler.vitals.VitalObservationType.*;

@Component
public class VitalObservationProviderAssembler {

  public List<CanonicalObservation> toCanonical(
      VitalsDBRecord row, List<VitalsUuidMappingDBRecord> mappings) {
    List<CanonicalObservation> observations = new ArrayList<>();

    for (VitalsUuidMappingDBRecord mapping : mappings) {
      VitalObservationType type =
          ObservationUuidUtil.getObservationFromCode(mapping.getResourcePath());
      if (type == null) continue;

      // 1. Initialize core metadata elements common to all vitals
      CanonicalObservation.CanonicalObservationBuilder builder =
          CanonicalObservation.builder()
              .id(mapping.getUuid().toString())
              .patientUuid(row.getPatientUuid().toString())
              .encounterUuid(row.getEncounterUuid().toString())
              .practitionerUuid(
                  row.getPractitionerUuid() != null ? row.getPractitionerUuid().toString() : null)
              .effectiveDateTime(row.getEffectiveDateTime())
              .lastUpdated(row.getLastUpdated())
              .version("1")
              .status("final") // Default vital sign operational lifecycle tracking state
              .profile(type.getProfile())
              .categories(List.of("vital-signs")) // Fixed classification tag anchor
              .code(type.getCode())
              .display(type.getDisplay())
              .codeSystem("http://loinc.org");

      CanonicalObservation observation = null;

      switch (type) {
        case BODY_WEIGHT:
          if (row.getWeight() != null) {
            observation =
                builder
                    .value(
                        CanonicalObservation.ObservationValue.builder()
                            .numericValue(
                                MeasurementUtils.lbToKg(row.getWeight())
                                    .doubleValue()) // Maps safely to Double
                            .unit(type.getUnit())
                            .unitSystem("http://unitsofmeasure.org")
                            .build())
                    .build();
          }
          break;

        case BODY_HEIGHT:
          if (row.getHeight() != null) {
            observation =
                builder
                    .value(
                        CanonicalObservation.ObservationValue.builder()
                            .numericValue(
                                MeasurementUtils.inchesToCm(row.getHeight()).doubleValue())
                            .unit(type.getUnit())
                            .unitSystem("http://unitsofmeasure.org")
                            .build())
                    .build();
          }
          break;

        case BLOOD_PRESSURE:
          if (row.getSystolic() != null || row.getDiastolic() != null) {
            List<CanonicalObservation.ObservationComponent> components = new ArrayList<>();

            if (row.getSystolic() != null) {
              components.add(
                  CanonicalObservation.ObservationComponent.builder()
                      .code(SYSTOLIC_BP.getCode())
                      .display(SYSTOLIC_BP.getDisplay())
                      .codeSystem("http://loinc.org")
                      .value(
                          CanonicalObservation.ObservationValue.builder()
                              .numericValue(row.getSystolic().doubleValue())
                              .unit(SYSTOLIC_BP.getUnit())
                              .unitSystem("http://unitsofmeasure.org")
                              .build())
                      .build());
            }

            if (row.getDiastolic() != null) {
              components.add(
                  CanonicalObservation.ObservationComponent.builder()
                      .code(DIASTOLIC_BP.getCode())
                      .display(DIASTOLIC_BP.getDisplay())
                      .codeSystem("http://loinc.org")
                      .value(
                          CanonicalObservation.ObservationValue.builder()
                              .numericValue(row.getDiastolic().doubleValue())
                              .unit(DIASTOLIC_BP.getUnit())
                              .unitSystem("http://unitsofmeasure.org")
                              .build())
                      .build());
            }
            // Tie component array back directly to parent panel capsule mapping block
            observation = builder.components(components).build();
          }
          break;

        case BODY_TEMPERATURE:
          if (row.getTemperature() != null) {
            observation =
                builder
                    .value(
                        CanonicalObservation.ObservationValue.builder()
                            .numericValue(
                                MeasurementUtils.fhToCelsius(row.getTemperature()).doubleValue())
                            .unit(type.getUnit())
                            .unitSystem("http://unitsofmeasure.org")
                            .build())
                    .build();
          }
          break;

        case BMI:
          if (row.getBmi() != null) {
            observation =
                builder
                    .value(
                        CanonicalObservation.ObservationValue.builder()
                            .numericValue(MeasurementUtils.fhToCelsius(row.getBmi()).doubleValue())
                            .unit(type.getUnit())
                            .unitSystem("http://unitsofmeasure.org")
                            .build())
                    .build();
          }
          break;

        default:
          throw new IllegalArgumentException(
              "Unsupported vital observation type constant: " + type);
      }

      if (observation != null) {
        observations.add(observation);
      }
    }

    return observations;
  }
}

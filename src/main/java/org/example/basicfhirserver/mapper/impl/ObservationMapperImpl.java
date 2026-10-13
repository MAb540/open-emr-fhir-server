package org.example.basicfhirserver.mapper.impl;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.example.basicfhirserver.mapper.ObservationMapper;
import org.example.basicfhirserver.model.CanonicalObservation;
import org.hl7.fhir.r4.model.*;
import org.springframework.stereotype.Component;

@Component
public class ObservationMapperImpl implements ObservationMapper {

  // Unique standard lookups for Blood Pressure panels matching your constant tracking tools
  private static final String BLOOD_PRESSURE_CODE = "85354-9";
  private static final String SYSTOLIC_CODE = "8480-6";
  private static final String DIASTOLIC_CODE = "8462-4";

  @Override
  public Observation toR4(CanonicalObservation canonicalObservation) {
    Observation observation = new Observation();

    // 1. Map Meta Parameters
    if (canonicalObservation.getProfile() != null) {
      observation.getMeta().addProfile(canonicalObservation.getProfile());
    }
    if (canonicalObservation.getLastUpdated() != null) {
      observation
          .getMeta()
          .setLastUpdated(
              Date.from(
                  canonicalObservation
                      .getLastUpdated()
                      .atZone(ZoneId.systemDefault())
                      .toInstant()));
    }
    if (canonicalObservation.getVersion() != null) {
      observation.getMeta().setVersionId(canonicalObservation.getVersion());
    }

    observation.setId(canonicalObservation.getId());

    // Map dynamic or fallback tracking states safely
    if (canonicalObservation.getStatus() != null) {
      try {
        observation.setStatus(
            Observation.ObservationStatus.fromCode(canonicalObservation.getStatus().toLowerCase()));
      } catch (IllegalArgumentException e) {
        observation.setStatus(Observation.ObservationStatus.FINAL);
      }
    } else {
      observation.setStatus(Observation.ObservationStatus.FINAL);
    }

    // 2. Map Dynamic Multiple Categories (Unified Strategy Pattern Resolution)
    if (canonicalObservation.getCategories() != null
        && !canonicalObservation.getCategories().isEmpty()) {
      for (String catCode : canonicalObservation.getCategories()) {
        String display =
            "social-history".equalsIgnoreCase(catCode) ? "Social History" : "Vital Signs";
        observation.addCategory(
            new CodeableConcept()
                .addCoding(
                    new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                        .setCode(catCode.toLowerCase())
                        .setDisplay(display)));
      }
    } else {
      // Default structural safety fallback context
      observation.addCategory(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem("http://terminology.hl7.org/CodeSystem/observation-category")
                      .setCode("vital-signs")
                      .setDisplay("Vital Signs")));
    }

    // 3. Map Identifying Codes
    String systemUri =
        canonicalObservation.getCodeSystem() != null
            ? canonicalObservation.getCodeSystem()
            : "http://loinc.org";
    observation.setCode(
        new CodeableConcept()
            .addCoding(
                new Coding()
                    .setSystem(systemUri)
                    .setCode(canonicalObservation.getCode())
                    .setDisplay(canonicalObservation.getDisplay())));

    // 4. Map Contextual Subject References
    if (canonicalObservation.getPatientUuid() != null) {
      observation.setSubject(new Reference("Patient/" + canonicalObservation.getPatientUuid()));
    }
    if (canonicalObservation.getEffectiveDateTime() != null) {
      observation.setEffective(
          new DateTimeType(canonicalObservation.getEffectiveDateTime().toString()));
    }
    if (canonicalObservation.getEncounterUuid() != null) {
      observation.setEncounter(
          new Reference("Encounter/" + canonicalObservation.getEncounterUuid()));
    }
    if (canonicalObservation.getPractitionerUuid() != null) {
      observation.setPerformer(
          List.of(new Reference("Practitioner/" + canonicalObservation.getPractitionerUuid())));
    }

    // =====================================================================
    // 5. Handle Multi-Component Panels (e.g. Blood Pressure)
    // =====================================================================
    if (BLOOD_PRESSURE_CODE.equals(canonicalObservation.getCode())
        || (canonicalObservation.getComponents() != null
            && !canonicalObservation.getComponents().isEmpty())) {

      List<Observation.ObservationComponentComponent> hapiComponents = new ArrayList<>();
      boolean isAllZero = true;

      if (canonicalObservation.getComponents() != null) {
        for (CanonicalObservation.ObservationComponent comp :
            canonicalObservation.getComponents()) {
          CanonicalObservation.ObservationValue compVal = comp.getValue();
          if (compVal != null && compVal.getNumericValue() != null) {
            if (compVal.getNumericValue() != 0.0) {
              isAllZero = false;
            }

            Observation.ObservationComponentComponent hapiComp =
                new Observation.ObservationComponentComponent();
            String compSys =
                comp.getCodeSystem() != null ? comp.getCodeSystem() : "http://loinc.org";

            hapiComp
                .getCode()
                .addCoding(
                    new Coding()
                        .setSystem(compSys)
                        .setCode(comp.getCode())
                        .setDisplay(comp.getDisplay()));

            UnitMapping uMap = mapUnitAndCode(compVal.getUnit());
            hapiComp.setValue(
                new Quantity()
                    .setValue(BigDecimal.valueOf(compVal.getNumericValue()))
                    .setUnit(uMap.unit())
                    .setSystem(
                        compVal.getUnitSystem() != null
                            ? compVal.getUnitSystem()
                            : "http://unitsofmeasure.org")
                    .setCode(uMap.code()));
            hapiComponents.add(hapiComp);
          }
        }
      }

      // Apply strict Data Absent Reason flags if composite analytics equate to zero
      if (isAllZero && !hapiComponents.isEmpty()) {
        observation.setDataAbsentReason(
            new CodeableConcept()
                .setText("Unknown")
                .addCoding(
                    new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/data-absent-reason")
                        .setCode("unknown")));
      } else {
        observation.setComponent(hapiComponents);
      }

      // =====================================================================
      // 6. Handle Polymorphic Singular Values (Quantity vs. CodeableConcept)
      // =====================================================================
    } else if (canonicalObservation.getValue() != null) {
      CanonicalObservation.ObservationValue obsVal = canonicalObservation.getValue();

      // Type A: Numeric Quantities (Vitals Metrics)
      if (obsVal.getNumericValue() != null) {
        UnitMapping uMap = mapUnitAndCode(obsVal.getUnit());
        observation.setValue(
            new Quantity()
                .setValue(BigDecimal.valueOf(obsVal.getNumericValue()))
                .setUnit(uMap.unit())
                .setSystem(
                    obsVal.getUnitSystem() != null
                        ? obsVal.getUnitSystem()
                        : "http://unitsofmeasure.org")
                .setCode(uMap.code()));

        // Type B: Coded Concepts (Social History matrix maps)
      } else if (obsVal.getCodedValues() != null && !obsVal.getCodedValues().isEmpty()) {
        CodeableConcept codeableConceptVal = new CodeableConcept();

        // Loop 1-to-1 matching out your nested Map<String, Map<String, String>> addCoding structure
        for (Map.Entry<String, Map<String, String>> entry : obsVal.getCodedValues().entrySet()) {
          Map<String, String> codingData = entry.getValue();

          Coding fhirCoding = new Coding();
          fhirCoding.setCode(codingData.get("code"));
          fhirCoding.setDisplay(codingData.get("description"));
          fhirCoding.setSystem(codingData.get("system"));

          codeableConceptVal.addCoding(fhirCoding);
          if (codingData.get("description") != null) {
            codeableConceptVal.setText(codingData.get("description"));
          }
        }
        observation.setValue(codeableConceptVal);

        // Type C: Plain narrative string fallbacks
      } else if (obsVal.getStringValue() != null) {
        observation.setValue(new StringType(obsVal.getStringValue()));
      }
    }

    return observation;
  }

  public record UnitMapping(String unit, String code) {}

  private UnitMapping mapUnitAndCode(String inputUnit) {
    if (inputUnit == null) return new UnitMapping("", "");

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

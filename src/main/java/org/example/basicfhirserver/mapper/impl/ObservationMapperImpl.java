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

  private static final String BLOOD_PRESSURE_CODE = "85354-9";

  @Override
  public Observation toR4(CanonicalObservation canonicalObservation) {
    Observation observation = new Observation();

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

    if (canonicalObservation.getCategories() != null
        && !canonicalObservation.getCategories().isEmpty()) {
      for (String catCode : canonicalObservation.getCategories()) {
        String display;
        switch (catCode.toLowerCase()) {
          case "social-history":
            display = "Social History";
            break;
          case "vital-signs":
            display = "Vital Signs";
            break;
          case "laboratory":
            display = "Laboratory";
            break;
          default:
            display = catCode.substring(0, 1).toUpperCase() + catCode.substring(1).toLowerCase();
            break;
        }
        observation.addCategory(
            new CodeableConcept()
                .addCoding(
                    new Coding()
                        .setSystem("http://hl7.org") //
                        .setCode(catCode.toLowerCase())
                        .setDisplay(display)));
      }
    } else {
      observation.addCategory(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem("http://hl7.org")
                      .setCode("vital-signs")
                      .setDisplay("Vital Signs")));
    }

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

    if (canonicalObservation.getPatientUuid() != null) {
      observation.setSubject(new Reference("Patient/" + canonicalObservation.getPatientUuid()));
    }
    if (canonicalObservation.getEffectiveDateTime() != null) {
      java.util.Date effectiveDate =
          java.util.Date.from(
              canonicalObservation
                  .getEffectiveDateTime()
                  .atZone(ZoneId.systemDefault())
                  .toInstant());
      observation.setEffective(new DateTimeType(effectiveDate));
    }
    if (canonicalObservation.getEncounterUuid() != null) {
      observation.setEncounter(
          new Reference("Encounter/" + canonicalObservation.getEncounterUuid()));
    }
    if (canonicalObservation.getPractitionerUuid() != null) {
      observation.setPerformer(
          List.of(new Reference("Practitioner/" + canonicalObservation.getPractitionerUuid())));
    }

    if (canonicalObservation.getReferenceRanges() != null
        && !canonicalObservation.getReferenceRanges().isEmpty()) {
      List<Observation.ObservationReferenceRangeComponent> hapiRanges = new ArrayList<>();

      for (CanonicalObservation.ReferenceRange canonicalRange :
          canonicalObservation.getReferenceRanges()) {
        Observation.ObservationReferenceRangeComponent hapiRange =
            new Observation.ObservationReferenceRangeComponent();
        UnitMapping rangeUnitMapping = mapUnitAndCode(canonicalRange.getUnit());

        if (canonicalRange.getLow() != null && !canonicalRange.getLow().isEmpty()) {
          try {
            hapiRange.setLow(
                new SimpleQuantity()
                    .setValue(new BigDecimal(canonicalRange.getLow()))
                    .setUnit(rangeUnitMapping.unit())
                    .setSystem("http://unitsofmeasure.org") //
                    .setCode(rangeUnitMapping.code()));
          } catch (NumberFormatException e) {
            hapiRange.setText(canonicalRange.getLow());
          }
        }

        if (canonicalRange.getHigh() != null && !canonicalRange.getHigh().isEmpty()) {
          try {
            hapiRange.setHigh(
                new SimpleQuantity()
                    .setValue(new BigDecimal(canonicalRange.getHigh()))
                    .setUnit(rangeUnitMapping.unit())
                    .setSystem("http://unitsofmeasure.org") //
                    .setCode(rangeUnitMapping.code()));
          } catch (NumberFormatException ignored) {
          }
        }
        hapiRanges.add(hapiRange);
      }
      if (!hapiRanges.isEmpty()) {
        observation.setReferenceRange(hapiRanges);
      }
    }

    if (canonicalObservation.getInterpretation() != null) {
      CanonicalObservation.InterpretationInfo interp = canonicalObservation.getInterpretation();
      String interpSys = interp.getCodeSystem() != null ? interp.getCodeSystem() : "http://hl7.org";
      observation.addInterpretation(
          new CodeableConcept()
              .addCoding(
                  new Coding()
                      .setSystem(interpSys)
                      .setCode(interp.getCode())
                      .setDisplay(interp.getDisplay())));
    }

    if (canonicalObservation.getSpecimenUuid() != null) {
      observation.setSpecimen(new Reference("Specimen/" + canonicalObservation.getSpecimenUuid()));
    }
    if (canonicalObservation.getNotes() != null && !canonicalObservation.getNotes().isEmpty()) {
      for (String noteText : canonicalObservation.getNotes()) {
        observation.addNote(new Annotation().setText(noteText));
      }
    }

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
      if (isAllZero && !hapiComponents.isEmpty()) {
        observation.setDataAbsentReason(
            new CodeableConcept()
                .setText("Unknown")
                .addCoding(new Coding().setSystem("hl7.org").setCode("unknown")));
      } else {
        observation.setComponent(hapiComponents);
      }

    } else if (canonicalObservation.getValue() != null) {
      CanonicalObservation.ObservationValue obsVal = canonicalObservation.getValue();
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
      } else if (obsVal.getCodedValues() != null && !obsVal.getCodedValues().isEmpty()) {
        CodeableConcept codeableConceptVal = new CodeableConcept();
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

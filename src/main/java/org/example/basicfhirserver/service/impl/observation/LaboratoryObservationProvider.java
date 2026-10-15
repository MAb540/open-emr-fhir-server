package org.example.basicfhirserver.service.impl.observation;

import java.util.*;
import java.util.stream.Collectors;
import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.query.resources.diagnosticreport.DiagnosticReportSearchQuery;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.procedure.ProcedureDBRecord;
import org.example.basicfhirserver.repository.jdbc.procedure.ProcedureRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class LaboratoryObservationProvider implements ObservationProvider {

  private final ProcedureRepository procedureRepository;

  private final String CATEGORY_LABORATORY = "laboratory";

  public LaboratoryObservationProvider(ProcedureRepository procedureRepository) {
    this.procedureRepository = procedureRepository;
  }

  @Override
  public boolean supports(ObservationSearchQuery searchQuery) {
    if (searchQuery.getCategory() != null && !searchQuery.getCategory().isEmpty()) {
      boolean matchesCategory =
          searchQuery.getCategory().stream()
              .anyMatch(cat -> CATEGORY_LABORATORY.equalsIgnoreCase(cat.getValue()));
      if (matchesCategory) return true;
    }

    return searchQuery.getCategory() == null && searchQuery.getCodes() == null;
  }

  @Override
  public Optional<CanonicalObservation> fetchById(UUID uuid) {
    return Optional.empty();
  }

  @Override
  public List<CanonicalObservation> fetch(ObservationSearchQuery query) {

    DiagnosticReportSearchQuery procedureQuery =
        DiagnosticReportSearchQuery.builder()
            .patientId(
                query.getPatientId() != null && !query.getPatientId().isEmpty()
                    ? query.getPatientId().get(0)
                    : null)
            .lastUpdated(query.getLastUpdated())
            .count(query.getCount())
            .offset(query.getOffset())
            .build();

    Page<ProcedureDBRecord> procedureDBRecords = procedureRepository.findLaboratory(procedureQuery);

    if (procedureDBRecords == null || procedureDBRecords.isEmpty()) {
      return Collections.emptyList();
    }

    List<CanonicalObservation> observations = new ArrayList<>();

    for (ProcedureDBRecord record : procedureDBRecords) {
      if (record.getReports() != null && !record.getReports().isEmpty()) {

        for (ProcedureDBRecord.ReportBlock report : record.getReports()) {
          if (report.getResults() != null && !report.getResults().isEmpty()) {

            for (ProcedureDBRecord.ResultBlock result : report.getResults()) {

              CanonicalObservation.CanonicalObservationBuilder obsBuilder =
                  CanonicalObservation.builder()
                      .id(
                          result.getUuid() != null
                              ? result.getUuid().toString()
                              : UUID.randomUUID().toString())
                      .version("1")
                      .status(result.getStatus() != null ? result.getStatus() : "final")
                      .lastUpdated(report.getDate())
                      .patientUuid(
                          record.getPatient() != null
                              ? record.getPatient().getUuid().toString()
                              : null)
                      .encounterUuid(
                          record.getEncounter() != null
                              ? record.getEncounter().getUuid().toString()
                              : null)
                      .practitionerUuid(
                          record.getProvider() != null
                              ? record.getProvider().getUuid() != null
                                  ? record.getProvider().getUuid().toString()
                                  : null
                              : null)
                      .profile(
                          "http://hl7.org/fhir/us/core/StructureDefinition/us-core-observation-lab")
                      .categories(List.of(CATEGORY_LABORATORY))
                      .code(result.getCode())
                      .display(result.getText())
                      .codeSystem("http://loinc.org")
                      .effectiveDateTime(report.getDate());

              String rawResult = result.getResult();
              if (rawResult != null && !rawResult.trim().isEmpty()) {
                CanonicalObservation.ObservationValue.ObservationValueBuilder valueBuilder =
                    CanonicalObservation.ObservationValue.builder();

                // Regex verification to differentiate numeric floats/integers from text
                // descriptions
                if (rawResult.trim().matches("-?\\d+(\\.\\d+)?")) {
                  valueBuilder.numericValue(Double.parseDouble(rawResult.trim()));
                  valueBuilder.unit(result.getUnits());
                  valueBuilder.unitSystem("http://unitsofmeasure.org");
                } else {
                  valueBuilder.stringValue(rawResult.trim());
                }
                obsBuilder.value(valueBuilder.build());
              }

              String rawRange = result.getRange();
              if (rawRange != null && !rawRange.trim().isEmpty()) {
                String rangeLow = null;
                String rangeHigh = null;

                if (rawRange.contains("-")) {
                  String[] rangeParts = rawRange.split("-", 2);
                  if (rangeParts.length == 2) {
                    rangeLow = rangeParts[0].trim();
                    rangeHigh = rangeParts[1].trim();
                  }
                } else {
                  rangeLow = rawRange.trim();
                }

                CanonicalObservation.ReferenceRange referenceRange =
                    CanonicalObservation.ReferenceRange.builder()
                        .low(rangeLow)
                        .high(rangeHigh)
                        .unit(result.getUnits())
                        .build();

                obsBuilder.referenceRanges(List.of(referenceRange));
              }

              if (result.getAbnormal() != null && !result.getAbnormal().isEmpty()) {
                CanonicalObservation.InterpretationInfo.InterpretationInfoBuilder interpBuilder =
                    CanonicalObservation.InterpretationInfo.builder()
                        .code(result.getAbnormal())
                        .display(
                            result.getResultAbnormalTitle() != null
                                ? result.getResultAbnormalTitle()
                                : "")
                        .codeSystem("http://hl7.org");

                if (result.getResultAbnormalCodes() != null
                    && !result.getResultAbnormalCodes().isEmpty()) {
                  interpBuilder.code(result.getResultAbnormalCodes());
                }

                obsBuilder.interpretation(interpBuilder.build());
              }

              List<String> observationNotes = new ArrayList<>();
              String primarySpecimenUuid = null;

              if (report.getSpecimens() != null && !report.getSpecimens().isEmpty()) {
                ProcedureDBRecord.SpecimenBlock firstSpecimen = report.getSpecimens().get(0);
                if (firstSpecimen.getUuid() != null) {
                  primarySpecimenUuid = firstSpecimen.getUuid().toString();
                }

                if (report.getSpecimens().size() > 1) {
                  String specimenIdentifiers =
                      report.getSpecimens().stream()
                          .map(
                              spec ->
                                  spec.getIdentifier() != null
                                      ? spec.getIdentifier()
                                      : (spec.getType() != null ? spec.getType() : "Unknown"))
                          .collect(Collectors.joining(", "));
                  observationNotes.add("Multiple specimens collected: " + specimenIdentifiers);
                }
              }

              if (result.getComments() != null && !result.getComments().trim().isEmpty()) {
                observationNotes.add(result.getComments().trim());
              }

              if (!observationNotes.isEmpty()) {
                obsBuilder.notes(observationNotes);
              }
              if (primarySpecimenUuid != null) {
                obsBuilder.specimenUuid(primarySpecimenUuid);
              }
              observations.add(obsBuilder.build());
            }
          }
        }
      }
    }
    return observations;
  }
}

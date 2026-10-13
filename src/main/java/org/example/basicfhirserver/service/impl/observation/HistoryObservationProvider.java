package org.example.basicfhirserver.service.impl.observation;

import java.util.*;
import java.util.stream.Collectors;
import org.example.basicfhirserver.model.CanonicalObservation;
import org.example.basicfhirserver.query.resources.SearchValue;
import org.example.basicfhirserver.query.resources.observation.ObservationSearchQuery;
import org.example.basicfhirserver.repository.jdbc.socialhistory.HistoryDBRecord;
import org.example.basicfhirserver.repository.jdbc.socialhistory.HistoryService;
import org.springframework.stereotype.Component;

@Component
public class HistoryObservationProvider implements ObservationProvider {
  private final HistoryService historyService;

  private static final String SMOKING_STATUS_LOINC_CODE = "72166-2";
  private static final String CATEGORY_SOCIAL_HISTORY = "social-history";

  public HistoryObservationProvider(HistoryService historyService) {
    this.historyService = historyService;
  }

  @Override
  public Optional<CanonicalObservation> fetchById(UUID uuid) {
    List<HistoryDBRecord> historyRecords = historyService.findHistoryById(uuid);
    if (historyRecords.isEmpty()) {
      return Optional.empty();
    }
    // Process and transform the matching record using our provider's converter bloc
    return Optional.empty();
  }

  @Override
  public boolean supports(ObservationSearchQuery searchQuery) {
    if (searchQuery.getCategory() != null && !searchQuery.getCategory().isEmpty()) {
      boolean matchesCategory =
          searchQuery.getCategory().stream()
              .anyMatch(cat -> CATEGORY_SOCIAL_HISTORY.equalsIgnoreCase(cat.getValue()));
      if (matchesCategory) return true;
    }

    if (searchQuery.getCodes() != null && !searchQuery.getCodes().isEmpty()) {
      return searchQuery.getCodes().stream()
          .anyMatch(code -> SMOKING_STATUS_LOINC_CODE.equals(code.getValue()));
    }

    return searchQuery.getCategory() == null && searchQuery.getCodes() == null;
  }

  @Override
  public List<CanonicalObservation> fetch(ObservationSearchQuery searchQuery) {
    List<HistoryDBRecord> records = historyService.findHistory(searchQuery);
    if (records == null || records.isEmpty()) {
      return Collections.emptyList();
    }

    List<CanonicalObservation> observations = new ArrayList<>();

    for (HistoryDBRecord record : records) {
      if (searchQuery.getCodes() != null && !searchQuery.getCodes().isEmpty()) {
        Set<String> requestedCodes =
            searchQuery.getCodes().stream().map(SearchValue::getValue).collect(Collectors.toSet());
        if (!requestedCodes.contains(SMOKING_STATUS_LOINC_CODE)) {
          continue;
        }
      }

      CanonicalObservation.CanonicalObservationBuilder builder =
          CanonicalObservation.builder()
              .id(record.getUuid() != null ? record.getUuid().toString() : null)
              .version("1")
              .status("final")
              .lastUpdated(record.getDate())
              .patientUuid(record.getPuuid() != null ? record.getPuuid().toString() : null)
              .practitionerUuid(
                  record.getProviderUuid() != null ? record.getProviderUuid().toString() : null)
              .profile("http://hl7.org")
              .categories(List.of(CATEGORY_SOCIAL_HISTORY))
              .code(SMOKING_STATUS_LOINC_CODE)
              .display("Tobacco smoking status NHIS")
              .codeSystem("http://loinc.org")
              .effectiveDateTime(record.getDate());

      CanonicalObservation.ObservationValue.ObservationValueBuilder valueBuilder =
          CanonicalObservation.ObservationValue.builder();
      boolean hasValue = false;

      if (record.getSmokingStatusCodes() != null) {
        @SuppressWarnings("unchecked")
        Map<String, Map<String, String>> codedValues =
            (Map<String, Map<String, String>>) (Object) record.getSmokingStatusCodes();
        valueBuilder.codedValues(codedValues);
        hasValue = true;
      }
      // Map supplementary quantitative metrics like packs per day if present
      //            record.getPacksPerDay() != null &&
      if (record.getPacksPerDay() > 0) {
        valueBuilder.numericValue((double) record.getPacksPerDay());
        valueBuilder.unit("{pack}/d");
        valueBuilder.unitSystem("http://unitsofmeasure.org");
        hasValue = true;
      }

      if (hasValue) {
        builder.value(valueBuilder.build());
        observations.add(builder.build());
      }
    }

    return observations;
  }
}

package org.example.basicfhirserver.service.assembler.condition;

import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionEncounterDiagnosisDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionHealthConcernDBRecord;
import org.example.basicfhirserver.repository.jdbc.condition.ConditionProblemListItemDBRecord;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class ConditionAssembler {

    private static final LocalDateTime UUID_CUTOVER_DATE = LocalDateTime.of(2025, 11, 15, 0, 0, 0);

    public static final String CATEGORY_PROBLEM_LIST = "problem-list-item";
    public static final String CATEGORY_ENCOUNTER_DIAGNOSIS = "encounter-diagnosis";
    public static final String CATEGORY_HEALTH_CONCERNS = "health-concerns";

    public ConditionCanonical toCanonical(ConditionProblemListItemDBRecord row) {
        return ConditionCanonical.builder()
                .id(row.getUuid().toString())
                .patientUuid(row.getPuuid().toString())
                .category(CATEGORY_PROBLEM_LIST)
                .clinicalStatus(computeClinicalStatus(row.getEnddate(), row.getOccurrence(), row.getOutcome()))
                .verificationStatus(row.getVerification() != null && !row.getVerification().isEmpty() ? row.getVerification() : "unconfirmed")
                .title(row.getTitle())
                .diagnosis(row.getDiagnosis())
                .comments(row.getComments())
                .onsetDate(row.getConditionDate())
                .begDate(row.getBegdate())
                .endDate(row.getEnddate())
                .lastUpdated(row.getLastUpdatedTime())
                .build();
    }

    public ConditionCanonical toCanonical(ConditionEncounterDiagnosisDBRecord row) {
        String computedId = getConditionFhirUuid(row);

        return ConditionCanonical.builder()
                .id(computedId)
                .patientUuid(row.getPuuid().toString())
                .category(CATEGORY_ENCOUNTER_DIAGNOSIS)
                .clinicalStatus(row.getResolved() != null && row.getResolved() == 1 ? "resolved" : "active")
                .verificationStatus(row.getVerification() != null && !row.getVerification().isEmpty() ? row.getVerification() : "confirmed")
                .title(row.getTitle())
                .diagnosis(row.getDiagnosis())
                .comments(row.getComments())
                .onsetDate(row.getDate())
                .begDate(row.getBegdate())
                .endDate(row.getEnddate())
                .lastUpdated(row.getLastUpdatedTime())
                .encounterUuid(row.getEncounterUuid().toString())
                .creatorUuid(row.getCreatorUuid().toString())
                .creatorNpi(row.getCreatorNpi())
                .updatorUuid(row.getUpdatorUuid().toString())
                .resolved(row.getResolved())
                .build();
    }

    public ConditionCanonical toCanonical(ConditionHealthConcernDBRecord row) {
        return ConditionCanonical.builder()
                .id(row.getUuid().toString())
                .patientUuid(row.getPuuid().toString())
                .category(CATEGORY_HEALTH_CONCERNS)
                .clinicalStatus(computeClinicalStatus(row.getEnddate(), row.getOccurrence(), row.getOutcome()))
                .verificationStatus(row.getVerification() != null && !row.getVerification().isEmpty() ? row.getVerification() : "unconfirmed")
                .title(row.getTitle())
                .diagnosis(row.getDiagnosis())
                .comments(row.getComments())
                .onsetDate(row.getConditionDate())
                .begDate(row.getBegdate())
                .endDate(row.getEnddate())
                .lastUpdated(row.getLastUpdatedTime())
                .healthConcernSubtype(row.getHealthConcernSubtype())
                .healthConcernSubtypeTitle(row.getHealthConcernSubtypeTitle())
                .build();
    }

    private String getConditionFhirUuid(ConditionEncounterDiagnosisDBRecord row) {
        LocalDateTime conditionTime = row.getDate() != null ? row.getDate() :
                (row.getBegdate() != null ? row.getBegdate() : LocalDateTime.of(1970, 1, 1, 0, 0, 0));

        if (!conditionTime.isBefore(UUID_CUTOVER_DATE)) {
            return row.getUuid().toString();
        }
        return row.getListsUuid();
    }


    private String computeClinicalStatus(LocalDateTime endDate, Integer occurrence, Integer outcome) {
        if (isClinicalStatusInactive(endDate)) {
            return "inactive";
        }

        int occurrenceVal = occurrence != null ? occurrence : 0;
        int outcomeVal = outcome != null ? outcome : 0;

        if (occurrenceVal == 1 || outcomeVal == 1) {
            return "resolved";
        } else if (occurrenceVal > 1) {
            return "recurrence";
        }

        return "active";
    }

    private boolean isClinicalStatusInactive(LocalDateTime endDate) {
        if (endDate == null) {
            return false;
        }
        try {
            ZonedDateTime targetDate = endDate.atZone(ZoneId.systemDefault());
            ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            return targetDate.isBefore(now);
        } catch (Exception e) {
            return false;
        }
    }

}

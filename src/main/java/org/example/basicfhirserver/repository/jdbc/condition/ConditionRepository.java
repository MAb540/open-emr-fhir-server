package org.example.basicfhirserver.repository.jdbc.condition;

import org.example.basicfhirserver.query.resources.condition.ConditionSearchCriteria;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;

import java.util.List;
import java.util.UUID;

public interface ConditionRepository {

    List<ConditionProblemListItemDBRecord> findConditionProblemListItemById(UUID uuid);

    List<ConditionProblemListItemDBRecord> findConditionProblemListItem(ConditionSearchQuery conditionSearchQuery);

    List<ConditionEncounterDiagnosisDBRecord> findConditionEncounterDiagnosisById(UUID uuid);

    List<ConditionEncounterDiagnosisDBRecord> findConditionEncounterDiagnosis(ConditionSearchQuery conditionSearchQuery);

    List<ConditionHealthConcernDBRecord> findConditionHealthConcernDiagnosisById(UUID uuid);

    List<ConditionHealthConcernDBRecord> findConditionHealthConcernDiagnosis(ConditionSearchQuery conditionSearchQuery);
}
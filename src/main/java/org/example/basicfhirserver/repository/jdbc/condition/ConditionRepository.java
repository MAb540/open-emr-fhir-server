package org.example.basicfhirserver.repository.jdbc.condition;

import java.util.List;
import java.util.UUID;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.springframework.data.domain.Page;

public interface ConditionRepository {

  List<ConditionProblemListItemDBRecord> findConditionProblemListItemById(UUID uuid);

  Page<ConditionProblemListItemDBRecord> findConditionProblemListItem(
      ConditionSearchQuery conditionSearchQuery);

  List<ConditionEncounterDiagnosisDBRecord> findConditionEncounterDiagnosisById(UUID uuid);

  Page<ConditionEncounterDiagnosisDBRecord> findConditionEncounterDiagnosis(
      ConditionSearchQuery conditionSearchQuery);

  List<ConditionHealthConcernDBRecord> findConditionHealthConcernDiagnosisById(UUID uuid);

  Page<ConditionHealthConcernDBRecord> findConditionHealthConcernDiagnosis(
      ConditionSearchQuery conditionSearchQuery);
}

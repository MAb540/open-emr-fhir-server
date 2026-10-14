package org.example.basicfhirserver.service;

import java.util.UUID;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.example.basicfhirserver.service.impl.condition.ConditionSearchResult;
import org.hl7.fhir.r4.model.Condition;

public interface ConditionService {

  Condition findById(UUID uuid);

  ConditionSearchResult find(ConditionSearchQuery conditionSearchQuery);
}

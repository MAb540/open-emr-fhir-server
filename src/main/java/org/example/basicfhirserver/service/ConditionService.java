package org.example.basicfhirserver.service;

import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchCriteria;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;

import java.util.List;
import java.util.UUID;

public interface ConditionService {

    ConditionCanonical findById(UUID uuid);

    List<ConditionCanonical> find(ConditionSearchQuery conditionSearchQuery);

}

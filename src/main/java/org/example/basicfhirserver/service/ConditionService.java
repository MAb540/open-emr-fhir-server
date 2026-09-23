package org.example.basicfhirserver.service;

import org.example.basicfhirserver.model.ConditionCanonical;
import org.example.basicfhirserver.query.resources.condition.ConditionSearchQuery;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface ConditionService {

    ConditionCanonical findById(UUID uuid);

    Page<ConditionCanonical> find(ConditionSearchQuery conditionSearchQuery);

}

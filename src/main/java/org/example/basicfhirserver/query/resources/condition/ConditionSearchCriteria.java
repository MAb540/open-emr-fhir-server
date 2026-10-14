package org.example.basicfhirserver.query.resources.condition;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConditionSearchCriteria {
  private ReferenceParam patient;
  private TokenParam category;
  private DateParam lastUpdated;
  private Integer count;
  private Integer offset;
  private Set<Include> includes;
}

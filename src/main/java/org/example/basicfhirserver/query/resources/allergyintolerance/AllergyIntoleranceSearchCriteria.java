package org.example.basicfhirserver.query.resources.allergyintolerance;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllergyIntoleranceSearchCriteria {
  private TokenParam id;
  private ReferenceParam patient;
  private DateParam lastUpdated;
  private Integer count;
  private Integer offset;
  private Set<Include> includes;
}

package org.example.basicfhirserver.query.resources.organization;

import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationSearchCriteria {
  private TokenParam id;
  private StringParam name;
  private DateParam lastUpdated;
  private Integer count;
  private Integer offset;
}

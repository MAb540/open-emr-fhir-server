package org.example.basicfhirserver.query.resources.coverage;

import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.TokenParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoverageSearchCriteria {
  private TokenParam id;
  private ReferenceParam patient;
  private TokenParam status;
  private Integer count;
  private Integer offset;
}

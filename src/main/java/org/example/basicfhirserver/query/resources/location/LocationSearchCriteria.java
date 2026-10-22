package org.example.basicfhirserver.query.resources.location;

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
public class LocationSearchCriteria {
  private TokenParam id;
  private StringParam name;
  private StringParam address;
  private StringParam addressCity;
  private StringParam addressState;
  private StringParam addressPostalCode;
  private DateParam lastUpdated;
  private Integer count;
  private Integer offset;
}

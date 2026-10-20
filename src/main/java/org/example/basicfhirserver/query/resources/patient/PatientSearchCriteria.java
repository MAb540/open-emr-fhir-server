package org.example.basicfhirserver.query.resources.patient;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.StringParam;
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
public class PatientSearchCriteria {
  private TokenParam id;
  private DateParam lastUpdated;
  private TokenParam identifier;
  private StringParam family;
  private StringParam given;
  private StringParam name;
  private DateParam birthdate;
  private DateParam deathDate;
  private TokenParam gender;
  private TokenParam telecom;
  private StringParam addressCity;
  private TokenParam active;
  private StringParam phone;
  private StringParam email;
  private Integer count;
  private Integer offset;
  private Set<Include> includes;
  private Set<Include> revIncludes;
}

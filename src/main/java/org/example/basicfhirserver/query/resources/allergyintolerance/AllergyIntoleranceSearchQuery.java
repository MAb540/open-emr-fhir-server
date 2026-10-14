package org.example.basicfhirserver.query.resources.allergyintolerance;

import java.time.LocalDateTime;
import java.util.Set;

import ca.uhn.fhir.model.api.Include;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.basicfhirserver.query.resources.SearchValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AllergyIntoleranceSearchQuery {
  private String id;
  private String patientId;
  private SearchValue<LocalDateTime> lastUpdated;
  private Integer count;
  private Integer offset;
  private Set<Include> includes;
}

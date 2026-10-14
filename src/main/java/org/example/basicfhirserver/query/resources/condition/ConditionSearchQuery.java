package org.example.basicfhirserver.query.resources.condition;

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
public class ConditionSearchQuery {
  private String patientId;
  private String category;
  private SearchValue<LocalDateTime> lastUpdated;
  private Integer count;
  private Integer offset;
  private Set<Include> includes;
}

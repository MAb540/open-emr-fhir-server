package org.example.basicfhirserver.query.resources.organization;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.basicfhirserver.query.resources.SearchValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrganizationSearchQuery {
  private List<String> organizationId;
  private SearchValue<String> name;
  private SearchValue<LocalDateTime> lastUpdated;
  private Integer count;
  private Integer offset;
}

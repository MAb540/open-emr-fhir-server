package org.example.basicfhirserver.query.resources.coverage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CoverageSearchQuery {
  private String id;
  private String patientId;
  private String status;
  private Integer count;
  private Integer offset;
}

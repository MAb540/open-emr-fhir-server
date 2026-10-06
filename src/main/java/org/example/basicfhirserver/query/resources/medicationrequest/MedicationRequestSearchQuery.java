package org.example.basicfhirserver.query.resources.medicationrequest;

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
public class MedicationRequestSearchQuery {
  private String patientId;
  private List<SearchValue<String>> intent;
  private String status;
  private Integer count;
  private Integer offset;
}

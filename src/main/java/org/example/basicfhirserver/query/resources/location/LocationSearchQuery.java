package org.example.basicfhirserver.query.resources.location;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.basicfhirserver.query.resources.SearchValue;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LocationSearchQuery {
  private String id;
  private SearchValue<String> name;
  private SearchValue<String> address;
  private SearchValue<String> addressCity;
  private SearchValue<String> addressState;
  private SearchValue<String> addressPostalCode;
  private SearchValue<LocalDateTime> lastUpdated;
  private Integer count;
  private Integer offset;
}

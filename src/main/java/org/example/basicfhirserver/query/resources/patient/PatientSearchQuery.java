package org.example.basicfhirserver.query.resources.patient;

import java.time.LocalDate;
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
public class PatientSearchQuery {

  private List<String> patientId;
  private SearchValue<LocalDateTime> lastUpdated;
  private String identifier;
  private SearchValue<String> firstName;
  private SearchValue<String> lastName;
  private SearchValue<String> name;
  private SearchValue<LocalDate> birthDate;
  private SearchValue<LocalDateTime> deathDate;
  private Integer count;
  private Integer offset;
}

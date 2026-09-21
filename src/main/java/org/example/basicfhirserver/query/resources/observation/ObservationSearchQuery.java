package org.example.basicfhirserver.query.resources.observation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.basicfhirserver.query.resources.SearchValue;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ObservationSearchQuery {

    private List<String> patientId;
    private String category;
    private List<SearchValue<String>> codes;
    private SearchValue<LocalDateTime> date;
    private SearchValue<LocalDateTime> lastUpdated;

}
